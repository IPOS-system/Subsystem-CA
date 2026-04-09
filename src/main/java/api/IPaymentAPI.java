package api;

import domain.Payment;
import domain.PaymentResult;

public interface IPaymentAPI {

	/**
	 * 
	 * @param payment
	 */
	PaymentResult Pay(Payment payment);

}