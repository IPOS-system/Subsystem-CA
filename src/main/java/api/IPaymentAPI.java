package api;

import domain.Payment;
import domain.PaymentResult;
import service.Result;

public interface IPaymentAPI {

	/**
	 * 
	 * @param payment
	 */
	Result Pay(Payment payment);

}