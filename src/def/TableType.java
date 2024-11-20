package def;

public enum TableType {
    
    FIXED_LIMIT("Fixed-Limit"),
    NO_LIMIT("No-Limit");
    
    private String name;
    
    TableType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

}
