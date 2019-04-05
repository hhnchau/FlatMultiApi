package phuthotech.vn.flatmultiapi;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Chau Huynh on 4/5/2019.
 */

public class MainTestFlatMap extends AppCompatActivity {
    private ApiService apiService;
    private static final String from = "DEL";
    private static final String to = "HYD";


    private TicketsAdapter mAdapter;
    private ArrayList<Ticket> ticketsList = new ArrayList<>();

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        apiService = ApiClient.getClient().create(ApiService.class);

        mAdapter = new TicketsAdapter(this, ticketsList, null);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(5), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        //Get Tickets
//        apiService.searchTickets(from, to)
//                //.toObservable()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeWith(new DisposableObserver<List<Ticket>>() {
//
//                    @Override
//                    public void onNext(List<Ticket> tickets) {
//                        int i = 0;
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });

        //Get Price
        apiService.searchTickets(from, to)
                //.toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Function<List<Ticket>, ObservableSource<Ticket>>() {
                    @Override
                    public ObservableSource<Ticket> apply(List<Ticket> lstTickets) throws Exception {

                        ticketsList.clear();
                        ticketsList.addAll(lstTickets);
                        mAdapter.notifyDataSetChanged();

                        //Send All Tickets
                        return Observable.fromIterable(lstTickets);
                    }
                })
                .flatMap(new Function<Ticket, ObservableSource<Ticket>>() {
                    @Override
                    public ObservableSource<Ticket> apply(final Ticket ticket) throws Exception {
                        //Receive Ticket
                        return apiService
                                .getPrice(ticket.getFlightNumber(), ticket.getFrom(), ticket.getTo())
                                //.toObservable()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .map(new Function<Price, Ticket>() {
                                    @Override
                                    public Ticket apply(Price price) throws Exception {
                                        ticket.setPrice(price);
                                        return ticket;
                                    }
                                });
                    }
                })
                .flatMap(new Function<Ticket, ObservableSource<Ticket>>() {
                    @Override
                    public ObservableSource<Ticket> apply(final Ticket ticket) throws Exception {
                        return apiService
                                .getPrice(ticket.getFlightNumber(), ticket.getFrom(), ticket.getTo())
                                //.toObservable()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .map(new Function<Price, Ticket>() {
                                    @Override
                                    public Ticket apply(Price price) throws Exception {
                                        ticket.setDesc(price);
                                        return ticket;
                                    }
                                });
                    }
                })
                .subscribeWith(new DisposableObserver<Ticket>() {

                    @Override
                    public void onNext(Ticket ticket) {

                        int position = ticketsList.indexOf(ticket);

                        if (position == -1) {
                            return;
                        }

                        ticketsList.set(position, ticket);
                        mAdapter.notifyItemChanged(position);
                    }

                    @Override
                    public void onError(Throwable e) {
                        int i = 0;
                    }

                    @Override
                    public void onComplete() {
                        int i = 0;
                    }
                });


    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }
    }
}
