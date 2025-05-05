package ba.sum.fsre.hackaton.user.adventure;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ba.sum.fsre.hackaton.R;

public class CitySpinnerAdapter extends BaseAdapter {

    private final Context context;
    private final String[] cities;
    private final int[] icons;

    public CitySpinnerAdapter(Context context, String[] cities, int[] icons) {
        this.context = context;
        this.cities = cities;
        this.icons = icons;
    }

    @Override
    public int getCount() {
        return cities.length;
    }

    @Override
    public Object getItem(int position) {
        return cities[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_item_with_icon, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.icon);
        TextView text = convertView.findViewById(R.id.text);

        icon.setImageResource(icons[position]);
        text.setText(cities[position]);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}