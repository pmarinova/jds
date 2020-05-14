package jds.app;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class UserActivity implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id = UUID.randomUUID().toString();
	
	private String userId;
	
	private String activity;
	
	private Date date = new Date();
	
	public UserActivity() {
	}

	public UserActivity(String userId, String activity) {
		this.userId = userId;
		this.activity = activity;
	}

	@Override
	public String toString() {
		return "UserActivity [id=" + id + ", userId=" + userId + ", activity=" + activity + ", date=" + date + "]";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
