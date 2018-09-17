package com.example.hawkerfinder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Adapter_Stall extends ArrayAdapter<String> {

    private Context mContext;
    private int mLayoutResId;
    private ArrayList<String> mStallName;
    private DatabaseHelper myDb;

    Adapter_Stall(Context context, int resource, ArrayList<String> stallNameArrayList) {
        super(context, resource, stallNameArrayList);
        this.mContext = context;
        this.mLayoutResId = resource;
        this.mStallName = stallNameArrayList;
        myDb = new DatabaseHelper(context);
    }

    @Override
    public String getItem(int position) {
        return super.getItem(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        PlaceHolder holder;

        //if we currently don't have a row View to reuse...
        if(row == null){
            //Create a new View
            LayoutInflater inflater = LayoutInflater.from(mContext);
            row = inflater.inflate(mLayoutResId,parent,false);

            holder = new PlaceHolder();

            holder.nameView = row.findViewById(R.id.stallNameTextView);
            holder.addressView = row.findViewById(R.id.stallAddressTextView);

            row.setTag(holder);
        }else{
            //Otherwise use an existing View
            holder = (PlaceHolder) row.getTag();
        }

        //Getting the data from the data array
        String string = mStallName.get(position);

        //Setup and reuse the same listener for each row
        holder.nameView.setOnClickListener(PopupListener);
        Integer rowPosition = position;
        holder.nameView.setTag(rowPosition);

        //setting the view to reflect the data we need to display
        holder.nameView.setText(string);
        holder.addressView.setText(myDb.getStallAddress(string));

        //returning the row (because this is called getView after all
        return row;

    }

    private View.OnClickListener PopupListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Integer viewPosition = (Integer) v.getTag();
            String string = mStallName.get(viewPosition);
            Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();
        }
    };

    private static class PlaceHolder {
        TextView nameView;
        TextView addressView;
    }
}
