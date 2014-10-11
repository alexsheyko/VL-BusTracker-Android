package ru.vlbustracker.adapters;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.vlbustracker.R;
import ru.vlbustracker.activities.MainActivity;
import ru.vlbustracker.models.Comment;

/**
 * Created by Шейко on 10.10.14.
 */
public  class MessageAdapter  extends BaseAdapter implements Filterable {

    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING = 1;

    private List<Pair<Comment, Integer>> messages;
    private List<Pair<Comment, Integer>> filter_messages;
    private LayoutInflater layoutInflater;

    public MessageAdapter(Activity activity) {
        layoutInflater = activity.getLayoutInflater();
        messages = new ArrayList<Pair<Comment, Integer>>();
        filter_messages = new ArrayList<Pair<Comment, Integer>>();
    }

    public void addMessage(Comment message, int direction) {
        messages.add(new Pair(message, direction));
        filter_messages.add(new Pair(message, direction));
        notifyDataSetChanged();
    }

    public void clearnMessage() {
        messages.clear();
        filter_messages.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
/*
    @Override
    public int getViewTypeCount() {
        return 2;
    }
*/
    /*
    @Override
    public int getItemViewType(int i) {
        return messages.get(i).second;
    }*/

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        //int direction = getItemViewType(i);

        //show message on left or right, depending on if
        //it's incoming or outgoing
        if (convertView == null) {
            int res = 0;
            //if (direction == DIRECTION_INCOMING) {
            //    res = R.layout.message_right;
            //} else if (direction == DIRECTION_OUTGOING) {
                res = R.layout.message_left;
            //}
            convertView = layoutInflater.inflate(res, viewGroup, false);
        }

        Comment message = messages.get(i).first;

        TextView txtMessage = (TextView) convertView.findViewById(R.id.txtMessage);
        txtMessage.setText(message.getText());

        txtMessage = (TextView) convertView.findViewById(R.id.txtSender);
        txtMessage.setText(message.getBusTxt());

        txtMessage = (TextView) convertView.findViewById(R.id.txtDate);
        txtMessage.setText(message.getDateStr()+" :"+message.getBusId());

        return convertView;
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                messages = (ArrayList<Pair<Comment, Integer>>) results.values;
                notifyDataSetChanged();
                notifyDataSetInvalidated();
                if (results.count == 0) {
                    //stops = (ArrayList<Stop>) results.values;
                    //notifyDataSetChanged();
                    //notifyDataSetInvalidated();
                }else {
                    //new ArrayList<Pair<Comment, Integer>>();
                    //notifyDataSetChanged();

                }
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "Set filter message " + constraint+" size:"+filter_messages.size()+"-"+messages.size());

                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    ArrayList<Pair<Comment, Integer>> FilteredArray = new ArrayList<Pair<Comment, Integer>>();
                    for (int i = 0; i < filter_messages.size(); i++) {
                         Comment com = filter_messages.get(i).first;
                         FilteredArray.add(new Pair(com, 1));
                    }
                    results.count = FilteredArray.size();
                    results.values = FilteredArray;

                    //synchronized(this) {
                        //results.values =  (ArrayList<Pair<Comment, Integer>>)  filter_messages;
                        //results.count = filter_messages.size();
                    //}
                }else {
                    constraint = constraint.toString().toLowerCase();

                    ArrayList<Pair<Comment, Integer>> FilteredArray = new ArrayList<Pair<Comment, Integer>>();

                    for (int i = 0; i < filter_messages.size(); i++) {
                        Comment com = filter_messages.get(i).first;
                        if (com.getBusId().equals(constraint)) {
                            FilteredArray.add(new Pair(com, 1));
                            //FilteredArray.add(com);
                            //if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "Set filter  =" + com.getID()+"-"+ messages.size()+"-"+i);
                        }
                    }
                    results.count = FilteredArray.size();
                    results.values = FilteredArray;
                }

                //if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "Set filter  " + results.count);
                return results;
            }
        };

        return filter;
    }
}
