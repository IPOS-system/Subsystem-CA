    package dao;

    import api_impl.DatabaseConnection;
    import domain.DiscountPlan;
    import domain.DiscountTier;

    import java.sql.*;
    import java.util.ArrayList;
    import java.util.List;

    public class DiscountPlanDAO {

        public DiscountPlan getCustomerDiscountPlan(String customerId) throws SQLException {
            String sql = """
                            SELECT plan_id, plan_name, plan_type, fixed_rate
                            FROM Discount_Plans
                            WHERE account_id = ?
                    """;

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, customerId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }

                    int planId = rs.getInt("plan_id");
                    if (rs.wasNull()) {
                        return null;
                    }

                    DiscountPlan plan = new DiscountPlan();
                    plan.setPlanId(planId);
                    plan.setPlanName(rs.getString("plan_name"));
                    plan.setPlanType(rs.getString("plan_type"));
                    plan.setFixedRate(rs.getBigDecimal("fixed_rate"));

                    if (DiscountPlan.TYPE_TIERED.equals(plan.getPlanType())) {
                        plan.setTiers(getTiersByPlanId(conn, planId));
                    }

                    return plan;
                }
            }
        }
        public void clearCustomerDiscountPlan(String customerId) throws SQLException {
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                try {
                    Integer planId = getCustomerPlanId(conn, customerId);

                    if (planId != null) {
                        String deletePlanSql = "DELETE FROM Discount_Plans WHERE plan_id = ?";
                        try (PreparedStatement ps = conn.prepareStatement(deletePlanSql)) {
                            ps.setInt(1, planId);
                            ps.executeUpdate();
                        }
                    }

                    conn.commit();
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        }

        public void saveFixedPlanForCustomer(String customerId, String planName, java.math.BigDecimal fixedRate) throws SQLException {
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                try {
                    Integer existingPlanId = getCustomerPlanId(conn, customerId);

                    int planId;
                    if (existingPlanId == null) {
                        //new one.
                        planId = insertPlan(conn, customerId, planName, DiscountPlan.TYPE_FIXED, fixedRate);
                    } else {
                        //replacing old plan
                        planId = existingPlanId;
                        updatePlan(conn, planId, planName, DiscountPlan.TYPE_FIXED, fixedRate);
                        deleteTiers(conn, planId);//delete the old tiers, not the plan though.
                        //sql doesnt care, so just alwasys delete tiers, even if there are none.
                    }

                    conn.commit();
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        }

        public void saveTieredPlanForCustomer(String customerId, String planName, List<DiscountTier> tiers) throws SQLException {
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                try {
                    Integer existingPlanId = getCustomerPlanId(conn, customerId);

                    int planId;
                    if (existingPlanId == null) {
                        //if there is no plan, make a new tiered one.
                        planId = insertPlan(conn, customerId, planName, DiscountPlan.TYPE_TIERED, null);
                    } else {
                        //if there is a plan, replace it with this one
                        planId = existingPlanId;
                        updatePlan(conn, planId, planName, DiscountPlan.TYPE_TIERED, null);
                        deleteTiers(conn, planId);
                        //delete all the old tiers. not the plan

                    }

                    //make a plan first, then insert the tiers. which come from the service who calls me
                    insertTiers(conn, planId, tiers);
                    conn.commit();
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        }

        //returns all the tiers in a plan/
        private List<DiscountTier> getTiersByPlanId(Connection conn, int planId) throws SQLException {
            String sql = """
                    SELECT tier_id, plan_id, min_amount, max_amount, discount_rate
                    FROM Discount_Tiers
                    WHERE plan_id = ?
                    ORDER BY min_amount
                    """;

            List<DiscountTier> tiers = new ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, planId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        DiscountTier tier = new DiscountTier();
                        tier.setTierId(rs.getInt("tier_id"));
                        tier.setPlanId(rs.getInt("plan_id"));
                        tier.setMinAmount(rs.getBigDecimal("min_amount"));
                        tier.setMaxAmount(rs.getBigDecimal("max_amount"));
                        tier.setDiscountRate(rs.getBigDecimal("discount_rate"));
                        tiers.add(tier);
                    }
                }
            }

            return tiers;
        }

        //gets the current customers plan./
        private Integer getCustomerPlanId(Connection conn, String customerId) throws SQLException {
            String sql = "SELECT plan_id FROM Discount_Plans WHERE account_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, customerId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("plan_id");
                    }
                    return null;
                }
            }
        }

        private int insertPlan(Connection conn, String customerId, String planName, String planType,
                               java.math.BigDecimal fixedRate) throws SQLException {
            String sql = """
            INSERT INTO Discount_Plans(account_id, plan_name, plan_type, fixed_rate)
            VALUES (?, ?, ?, ?)
            """;

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, customerId);
                ps.setString(2, planName);
                ps.setString(3, planType);

                if (fixedRate == null) {
                    ps.setNull(4, Types.DECIMAL);
                } else {
                    ps.setBigDecimal(4, fixedRate);
                }

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }

            throw new SQLException("coudl not create discount plan.");
        }

        private void updatePlan(Connection conn, int planId, String planName, String planType, java.math.BigDecimal fixedRate) throws SQLException {
            String sql = """
                    UPDATE Discount_Plans
                    SET plan_name = ?, plan_type = ?, fixed_rate = ?
                    WHERE plan_id = ?
                    """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, planName);
                ps.setString(2, planType);

                if (fixedRate == null) {
                    ps.setNull(3, Types.DECIMAL);
                } else {
                    ps.setBigDecimal(3, fixedRate);
                }

                ps.setInt(4, planId);
                ps.executeUpdate();
            }
        }


        private void deleteTiers(Connection conn, int planId) throws SQLException {
            String sql = "DELETE FROM Discount_Tiers WHERE plan_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, planId);
                ps.executeUpdate();
            }
        }

        private void insertTiers(Connection conn, int planId, List<DiscountTier> tiers) throws SQLException {
            String sql = """
                    INSERT INTO Discount_Tiers(plan_id, min_amount, max_amount, discount_rate)
                    VALUES (?, ?, ?, ?)
                    """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (DiscountTier tier : tiers) {
                    ps.setInt(1, planId);
                    ps.setBigDecimal(2, tier.getMinAmount());

                    if (tier.getMaxAmount() == null) {
                        ps.setNull(3, Types.DECIMAL);
                    } else {
                        ps.setBigDecimal(3, tier.getMaxAmount());
                    }

                    ps.setBigDecimal(4, tier.getDiscountRate());
                    ps.addBatch();
                }

                ps.executeBatch();
            }
        }
    }