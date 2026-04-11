package service;

import dao.MerchantInfoDAO;
import domain.MerchantInfo;

public class MerchantInfoService {

    private final MerchantInfoDAO dao = new MerchantInfoDAO();

    public MerchantInfo load() {
        return dao.getInfo();
    }

    public boolean save(MerchantInfo info) {
        if (info.getPharmacyName() == null || info.getPharmacyName().trim().isEmpty())
            throw new IllegalArgumentException("Pharmacy name is required");
        // Email format checking could be added here if desired.
        return dao.save(info);
    }
}
