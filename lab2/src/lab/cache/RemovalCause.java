package lab.cache;

// Чому запис зник з кешу.
public enum RemovalCause {
    // видалено явно через remove() або clear()
    EXPLICIT,
    // значення замінено новим через put()
    REPLACED,
    // минув час життя (expiry)
    EXPIRED,
    // витіснено через ліміт розміру (eviction)
    EVICTED
}
