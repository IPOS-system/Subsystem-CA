package service;

import api_impl.IAccInfoAPIService;
import api_impl.SAService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dao.CatalogueDAO;
import domain.Item;

import java.util.ArrayList;
import java.util.List;

public class CatalogueService {
    private final CatalogueDAO catalogueDAO;
    private final IAccInfoAPIService iAccInfoAPIService;
    private final ObjectMapper objectMapper;
    private final SAService saService;


    public CatalogueService (SAService saService){
        this.catalogueDAO = new CatalogueDAO();
        this.iAccInfoAPIService= new IAccInfoAPIService(saService);
        this.objectMapper = new ObjectMapper();
        this.saService= saService;

    }
    public String formatItemId(String itemId) {
        return itemId.replaceAll("\\D", "");
    }
    public Result syncCatalogue() {
        if (saService.isConnected()) {

            Result res = iAccInfoAPIService.sendCatalogue();

            if (!res.isSuccess()) {
                return res;
            }

            try {
                JsonNode json = objectMapper.readTree(res.getMessage());
                List<Item> items = new ArrayList<>();


                for (JsonNode product : json) {
                    //System.out.println(formatItemId(product.get("productId").asText()));

                    Item item = new Item(

                            formatItemId(product.get("productId").asText()),
                            product.get("description").asText(),
                            product.get("packageType").asText(),
                            product.get("unit").asText(),
                            product.get("unitsPerPack").asInt(),
                            product.get("unitPrice").decimalValue(),
                            product.get("availability").asInt(),
                            0,
                            0
                    );
                    items.add(item);
                }

                catalogueDAO.updateCatalogue(items);
                return Result.success("Catalogue synced");

            } catch (Exception e) {
                return Result.fail(e.getMessage());
            }
        }
        return Result.fail("not connected to SA");
    }


    public List<Item> findAll() {
        Result sync = syncCatalogue();

        if (!sync.isSuccess()) {
            System.out.println("Catalogue sync failed: IPOS SA OFFLINE..RETURN NORMAL CATALOGUE" + sync.getMessage());
        }

        return catalogueDAO.findAll();
    }

    public Item findById(String itemId) {
        return catalogueDAO.findById(itemId);
    }
}
