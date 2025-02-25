package com.udit.umbrellanotify;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.udit.umbrellanotify.Common.Common;
import com.udit.umbrellanotify.Model.WeatherResult;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class TodayWeatherFragment extends Fragment {

    static TodayWeatherFragment instance;


    ImageView img_weather;
    TextView txt_city_name,txt_humidity,txt_sunrise,txt_sunset,txt_pressure,txt_temperature,txt_description,txt_date_time,txt_wind,txt_geo_coord;
    LinearLayout weather_panel;
    ProgressBar loading;
    CompositeDisposable compositeDisposable;
    IOpenWeatherMap mService;
    public static TodayWeatherFragment getInstance(){
        if(instance==null)
            instance=new TodayWeatherFragment();
        return instance;
    }

    public TodayWeatherFragment() {
    compositeDisposable=new CompositeDisposable();
        Retrofit retrofit=RetroFitClient.getInstance();
        mService=retrofit.create(IOpenWeatherMap.class);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView= inflater.inflate(R.layout.fragment_today_weather,container,false);
        img_weather=itemView.findViewById(R.id.img_weather);
        txt_city_name=itemView.findViewById(R.id.txt_city_name);
        txt_humidity=itemView.findViewById(R.id.txt_humidity);
        txt_sunrise=itemView.findViewById(R.id.txt_sunrise);
        txt_sunset=itemView.findViewById(R.id.txt_sunset);
        txt_pressure=itemView.findViewById(R.id.txt_pressure);
        txt_temperature=itemView.findViewById(R.id.txt_temperature);
        txt_description=itemView.findViewById(R.id.txt_description);
        txt_wind=itemView.findViewById(R.id.txt_wind);
        txt_date_time=itemView.findViewById(R.id.txt_date_time);
        txt_geo_coord=itemView.findViewById(R.id.txt_geo_coord);

        weather_panel=itemView.findViewById(R.id.weather_panel);
        loading=itemView.findViewById(R.id.loading);

        getWeatherInformation();

        return itemView;
    }

    private void getWeatherInformation() {
        compositeDisposable.add(mService.getWeatherByLating(String.valueOf(Common.current_location.getLatitude()),String.valueOf(Common.current_location.getLongitude()),
                Common.APP_ID,"metric").subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {
                        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w/").append(weatherResult.getWeather().get(0).getIcon())
                        .append(".png").toString()).into(img_weather);
                        txt_city_name.setText(weatherResult.getName());
                        txt_description.setText(new StringBuilder("Weather in ")
                        .append(weatherResult.getName().toString()));
                        txt_temperature.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getTemp())).append("°C").toString());
                        txt_date_time.setText(Common.convertUnixToDate(weatherResult.getDt()));
                        txt_pressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append("hpa").toString());
                        txt_humidity.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append("%").toString());
                        txt_sunrise.setText(Common.UnixToHour( weatherResult.getSys().getSunrise()));
                        txt_sunset.setText(Common.UnixToHour( weatherResult.getSys().getSunset()));
                        txt_geo_coord.setText(new StringBuilder(weatherResult.getCoord().toString()).toString());
                        txt_wind.setText(new StringBuilder(String.valueOf(weatherResult.getWind().getSpeed())).append("kmph").append(" , ").append(weatherResult.getWind().getDeg()).append("°").toString());

                        weather_panel.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);


                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getActivity(),""+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }));
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    @Override
    public void onStop() {
        super.onStop();
        compositeDisposable.clear();
    }
}