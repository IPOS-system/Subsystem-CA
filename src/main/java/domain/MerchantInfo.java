package domain;


public class MerchantInfo {
    private String pharmacyName;
    private String address;
    private String email;
    // absolute path or URL of the logo image (optional)
    private String logoPath;

    public MerchantInfo() {}


    public String getPharmacyName()          { return pharmacyName; }
    public void setPharmacyName(String val)  { this.pharmacyName = val; }

    public String getAddress()               { return address; }
    public void setAddress(String val)        { this.address = val; }

    public String getEmail()                 { return email; }
    public void setEmail(String val)         { this.email = val; }

    public String getLogoPath()              { return logoPath; }
    public void setLogoPath(String val)     { this.logoPath = val; }
}
