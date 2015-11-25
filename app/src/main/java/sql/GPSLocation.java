package sql;

/**
 * This class represents an event report
 * 
 * @author Abhinav Parate
 */
public class GPSLocation {

	/** Report id */
	public int id = -1;
	
	/** timestamp of the event */
	public long timestamp;
	
	/** latitude */
	public double latitude;
	
	/** longitude */
	public double longitude;
	
	/** accuracy */
	public float accuracy;

		
	/**
	 * Constructor without id
	 * @param timestamp
	 * @param lat
	 * @param lng
	 * @param accuracy
	 */
	public GPSLocation(long timestamp, double lat, double lng, float accuracy) {
		this.timestamp = timestamp;
		this.latitude = lat;
		this.longitude = lng;
		this.accuracy = accuracy;
	}
	
	/**
	 * Constructor with report id
	 * @param id
	 * @param timestamp
	 * @param lat
	 * @param lng
	 * @param accuracy
	 */
	public GPSLocation(int id, long timestamp, double lat, double lng, float accuracy) {
		this.id =id;
		this.timestamp = timestamp;
		this.latitude = lat;
		this.longitude = lng;
		this.accuracy = accuracy;
	}
	
	/**
	 * Empty Constructor
	 */
	public GPSLocation() {
		
	}
	
	public boolean equals(GPSLocation r){
		return this.id == r.id;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public float getAccuracy() {
		return accuracy;
	}
	
	public int getId() {
		return id;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

}