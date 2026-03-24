package api;

import domain.Email;

public interface IEmailAPI {

	/**
	 * 
	 * @param email
	 */
	boolean sendEmail(Email email);

}