package station;

public enum PackageOrder {

	/**
	 * Format und Semangtik der Nachrichtenpackete
	 * 
	 * Byte 0: Stationsklasse ('A' oder 'B')
	 * 
	 * Byte 1 – 24: Nutzdaten. (Darin Byte 1 – 10: Name der sendenden Station.)
	 * 
	 * Byte 25: Nummer des Slots, in dem die Station im nächsten Frame senden
	 * wird.
	 * 
	 * Byte 26 – 33: Zeitpunkt, zu dem dieses Paket gesendet wurde. Einheit:
	 * Millisekunden seit dem 1.1.1970 als 8-Byte Integer, Big Endian.
	 * 
	 * 
	 * Gesamtlänge: 34 Bytes
	 * 
	 */

	STATION_CLASS(0, 1), 
	DATA(STATION_CLASS.to(), 24), 
	RESERVED_SLOT(DATA.to(), 1), 
	SEND_TIME(RESERVED_SLOT.to(), 8);

	private int length;
	private int from;
	private int to;

	private PackageOrder(int from, int length) {
		this.from = from;
		this.length = length;
	}

	public int length() {
		return length;
	}

	public int from() {
		return from;
	}

	public int to() {
		return from + length;
	}

}
