package ru.vlbustracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.vlbustracker.R;
import ru.vlbustracker.models.Route;

public class RouteAdapter extends BaseAdapter implements Filterable {

    private final LayoutInflater mInflater;
    private final View.OnClickListener textOnClickListener;
    private final CompoundButton.OnCheckedChangeListener checkBoxOnCLickListener;
    private ArrayList<Route> routes = new ArrayList<Route>();
    private ArrayList<Route> filterRoutes = new ArrayList<Route>();

    // Constructor. Just used to initialize variables.
    public RouteAdapter(Context context, ArrayList<Route> mRoute, View.OnClickListener listener, CompoundButton.OnCheckedChangeListener cbListener) {
        textOnClickListener = listener;
        checkBoxOnCLickListener = cbListener;
        mInflater = LayoutInflater.from(context);
        routes = mRoute;
        filterRoutes = mRoute;
    }

    // Number of elements feeding into this adapter.
    public int getCount() {
        return routes.size();
    }

    // Return the object at the given position.
    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    // getView gets the view for the given position (just a single element of our List View).
    // In this case, our view is just a compound button and a text view. This method populates
    // the CompoundButton and TextView with the proper values (by using the given position) from
    // our list of stops.
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.stop_list_item1, parent, false);
            if (convertView != null) {
                holder = new ViewHolder();
                // Get the specific views and store them in a ViewHolder.
                holder.text = (TextView) convertView.findViewById(R.id.stop_text);
                holder.checkbox = (CheckBox) convertView.findViewById(R.id.stop_checkbox);

                // Set their onClickListeners to the ones provided in the constructor.
                holder.checkbox.setOnCheckedChangeListener(checkBoxOnCLickListener);
                holder.text.setOnClickListener(textOnClickListener);
                convertView.setTag(holder);
            }
            else return new View(null);   // Should never reach here.
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Set their tags as the stop they represent. Used to pull out the stop on the other side (MainActivity).
        holder.text.setTag(routes.get(position));
        holder.checkbox.setTag(routes.get(position));

        // Update the display values of the views to reflect the object they represent.
        holder.text.setText(routes.get(position).getLongName());
        holder.checkbox.setChecked(false);

        return convertView;
    }

    class ViewHolder {
        CheckBox checkbox;
        TextView text;
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.count == 0) {
                    //stops = (ArrayList<Stop>) results.values;
                    //notifyDataSetChanged();
                    notifyDataSetInvalidated();
                }else {
                    routes = (ArrayList<Route>) results.values;
                    notifyDataSetChanged();
                }
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.values = filterRoutes;
                    results.count = filterRoutes.size();
                }else {
                    List<Route> FilteredArray = new ArrayList<Route>();

                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < filterRoutes.size(); i++) {
                        Route rout = filterRoutes.get(i);
                        if (rout.getLongName().toLowerCase().startsWith(constraint.toString())) {
                            FilteredArray.add(rout);
                        }
                    }
                    results.count = FilteredArray.size();
                    results.values = FilteredArray;
                }

                return results;
            }
        };

        return filter;
    }
}
