package tacos.model;

public enum SpiceLevel {
    NONE(0, "Sin picante"),
    MILD(1, "Suave "),
    MEDIUM(2, "Medio"),
    HOT(3, "Picante"),
    INFERNAL(4, "Incendiario");

    private final int rank;
    private final String description;

    SpiceLevel(int rank, String description) {
        this.rank = rank;
        this.description = description;
    }

    public int getRank() { return rank; }
    
    public static SpiceLevel fromRank(int rank) {
        if (rank >= 4) return INFERNAL;
        if (rank == 3) return HOT;
        if (rank == 2) return MEDIUM;
        if (rank == 1) return MILD;
        return NONE;
    }
}
