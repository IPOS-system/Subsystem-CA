package service;

import java.time.LocalDate;

public class TimeService {

    private LocalDate virtualDate = null;

    public LocalDate today() {
        return (virtualDate != null) ? virtualDate : LocalDate.now();
    }

    public void setVirtualDate(LocalDate date) {
        this.virtualDate = date;
    }

    public void reset() {
        this.virtualDate = null;
    }

    public boolean isVirtual() {
        return virtualDate != null;
    }
}