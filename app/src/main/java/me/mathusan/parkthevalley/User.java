package me.mathusan.parkthevalley;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Mathu on 2017-01-07.
 */

public class User  implements Serializable {

    private String email;
    private String name;
    private String phone;

    private List<Spot> spots;
//    private List<Marker> markers;


    public User() {
      /*Blank default constructor essential for Firebase*/
    }


    /*protected User(Parcel in) {
        email = in.readString();
        name = in.readString();
        phone = in.readString();
        price = in.readDouble();
        spots = in.readList(spots, new SpotCreator());
    }*/

    /*public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };*/

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Spot> getSpots() {
        return spots;
    }

    public void setSpots(List<Spot> spots) {
        this.spots = spots;
    }

//    public List<Marker> getMarkers(){
//        return markers;
//    }
//
//    public void setMarkers(List<Marker> markers){
//        this.markers = markers;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public int hashCode(){
        double result = 31 * ((email == null) ? 17 : email.hashCode())
                      * 31 * ((name == null) ? 17 : name.hashCode())
                      * 31 * (phone.equals("") ? 17 : phone.hashCode());
        return (int) result;
    }

    @Override
    public boolean equals(Object obj){
        if( !(obj instanceof User)) return false;

        User u = (User) obj;
        if(u.getEmail().equals(email)
                &&
                u.getName().equals(name)){
            return true;
        }
        return false;
    }
    public class SpotCreator extends ClassLoader {


    }
}
