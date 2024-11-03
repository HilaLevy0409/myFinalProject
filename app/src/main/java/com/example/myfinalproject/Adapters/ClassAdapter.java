package Adapters;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.myfinalproject.R;

import java.util.Random;

public class ClassAdapter extends BaseAdapter {
    private Context context;
    private String[] classes;
    private Random random;

    public ClassAdapter(Context context, String[] classes) {
        this.context = context;
        this.classes = classes;
        this.random = new Random();
    }

    @Override
    public int getCount() {
        return classes.length;
    }

    @Override
    public Object getItem(int position) {
        return classes[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        }

        TextView textViewClass = convertView.findViewById(R.id.textViewClass);
        textViewClass.setText(classes[position]);

        // Generate a random color for the background
        int color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        convertView.setBackgroundColor(color);

        return convertView;
    }
}
