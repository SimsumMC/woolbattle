package woolbattle.woolbattle;

public class Enums {

    public enum PerkType {
        FIRST_ACTIVE(1),
        SECOND_ACTIVE(2),
        PASSIVE(3);

        public final Integer value;

        PerkType(Integer value) {
            this.value = value;
        }
    }
}
