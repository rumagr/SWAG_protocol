package src.fachwert;

public enum TaskArt {
    MESSAGE, MESSAGE_SELF, MESSAGE_OTHERS, CR, CRR, SCC, SCCR, STU,
    CONNECTED_USERS,
    TIMER_EXPIRED, TIMER_START, TIMER_STOP, TIMER_TABLE_UPDATE_START, TIMER_TABLE_UPDATE_EXPIRED,
    CONNECT_TO, SEND_MESSAGE_TO, GET_CONNECTED_USERS, EXIT;


    public static TaskArt intToTaskArt(int code) {
        return switch (code) {
            case 1 -> MESSAGE;
            case 2 -> CR;
            case 3 -> CRR;
            case 4 -> SCC;
            case 5 -> SCCR;
            case 6 -> STU;
            default -> null;
        };
    }
}
