package volosyuk.easybizcard.adapters;

import static volosyuk.easybizcard.utils.CountryManager.PHONE_CODES;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.blongho.country_data.Country;

import java.util.List;

import volosyuk.easybizcard.R;

public class CountryAdapter extends ArrayAdapter<Country> {
    private Context context;
    private List<Country> countries;

    public CountryAdapter(Context context, List<Country> countries) {
        super(context, 0, countries);
        this.context = context;
        this.countries = countries;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Для свернутого состояния показываем только код страны
        TextView countryCodeText;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_spinner_country, parent, false);
        }

        countryCodeText = convertView.findViewById(R.id.country_code);
        TextView countryNameText = convertView.findViewById(R.id.country_name);
        ImageView countryFlagImage = convertView.findViewById(R.id.country_flag);
        countryNameText.setVisibility(View.GONE);
        countryFlagImage.setVisibility(View.GONE);

        Country country = getItem(position);

        // Устанавливаем только код страны для свернутого состояния
        countryCodeText.setText(PHONE_CODES.getOrDefault(country.getAlpha2(), "+2"));

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        // Для развернутого состояния показываем флаг и название страны
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_spinner_country, parent, false);
        }

        Country country = getItem(position);

        TextView countryNameText = convertView.findViewById(R.id.country_name);
        ImageView countryFlagImage = convertView.findViewById(R.id.country_flag);

        countryNameText.setText(country.getName());
        countryFlagImage.setImageResource(country.getFlagResource());

        // Устанавливаем код страны для развернутого состояния
        TextView countryCodeText = convertView.findViewById(R.id.country_code);
        countryCodeText.setText(PHONE_CODES.getOrDefault(country.getAlpha2(), "+2"));

        return convertView;
    }
}


