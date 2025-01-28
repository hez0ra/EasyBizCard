package volosyuk.easybizcard.utils;

import android.app.Application;
import com.blongho.country_data.World;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Инициализация библиотеки World Country Data
        World.init(getApplicationContext());
    }
}
