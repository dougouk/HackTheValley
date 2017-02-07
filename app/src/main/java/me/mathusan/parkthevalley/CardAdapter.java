package me.mathusan.parkthevalley;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * Created by Dan on 08/01/2017.
 */

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.MyViewHolder> {

    private Context mContext;
    private List<Spot> spotList;
    String name, phone;
    double price;
    public Switch toggle;
    public User user;
    public Firebase firebase;
    public String key;
    public DatabaseReference database;
    final public static String FIREBASE_URL = "https://fir-parkthevalley.firebaseio.com/";


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, name, phone, money;
        public ImageView nameIcon, phoneIcon, moneyIcon;

        public MyViewHolder(View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.title);

            name = (TextView) view.findViewById(R.id.name);
            nameIcon = (ImageView) view.findViewById(R.id.nameIcon);

            money = (TextView) view.findViewById(R.id.price);
            moneyIcon = (ImageView) view.findViewById(R.id.priceIcon);

            phone = (TextView) view.findViewById(R.id.phone);
            phoneIcon = (ImageView) view.findViewById(R.id.phoneIcon);

            toggle = (Switch) view.findViewById(R.id.toggle_switch);


            database = FirebaseDatabase.getInstance().getReference();
            Firebase.setAndroidContext(mContext);
            firebase = new Firebase(FIREBASE_URL);

        }
    }


    public CardAdapter(Context mContext, List<Spot> spotList, String name, String phone, User user, String key) {
        this.mContext = mContext;
        this.spotList = spotList;
        this.name = name;
        this.phone = phone;
        this.price = price;
        this.key = key;
        this.user = user;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_item, parent, false);
        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Spot spot = spotList.get(position);
        toggle.setChecked(spot.getOpen());
        holder.phone.setText(phone);
        holder.name.setText(name);
        holder.money.setText(Double.toString(spot.getPrice()));
        holder.title.setText(Double.toString(spot.getLat()) + ", " + Double.toString(spot.getLng()));
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean bool = spotList.get(position).getOpen();

                User u = user;
                Spot s = u.getSpots().get(position);
                s.setOpen(!bool);
                database.child("availableSpots")
                .child(u.getName())
                .child("spots")
                .setValue(spotList);

//                firebase.getRef().child("Open").setValue(!bool);


            }
        });
    }


    @Override
    public int getItemCount() {
        return spotList.size();
    }
}