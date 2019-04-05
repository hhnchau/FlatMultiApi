package phuthotech.vn.flatmultiapi;

/**
 * Created by ravi on 02/03/18.
 */

import java.util.List;


import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("airline-tickets.php")
    Observable<List<Ticket>> searchTickets(@Query("from") String from, @Query("to") String to);

    @GET("airline-tickets-price.php")
    Observable<Price> getPrice(@Query("flight_number") String flightNumber, @Query("from") String from, @Query("to") String to);
}
