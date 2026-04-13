package api;

import domain.Email;
import service.Result;

public interface IEmailAPI {

	/**
	 * 
	 * @param email
	 */
	Result sendEmail(Email email);

}