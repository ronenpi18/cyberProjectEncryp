package project.unitato.encrypchat;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatList extends ArrayAdapter<String>{
	private final Activity context;
	private final ArrayList<String> web;
	private final ArrayList<Integer> imageId;
    private final ArrayList<String> times;
    private Typeface textTypeface;
    private int textSize;
	
	public ChatList(Activity context, ArrayList<String> web, ArrayList<Integer> imageId, ArrayList<String> times, Typeface font, int textSize) {
        super(context, R.layout.chat_single, web);
        this.context = context;
        this.textTypeface = font;
        this.textSize= textSize;
        this.web = web;
        this.imageId = imageId;
        this.times = times;
    }

	
	
	
	@Override
	public void add(String object) {
		this.web.add(object);
		this.imageId.add((Integer)imageId.toArray()[0]);
		notifyDataSetChanged();
	}




	@Override
	public View getView(int position, View view, ViewGroup parent) {

		LayoutInflater inflater = context.getLayoutInflater();
		View rowView= inflater.inflate(R.layout.chat_single, null, true);
		TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        TextView timeTv = (TextView) rowView.findViewById(R.id.time_tv);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        timeTv.setText((String) times.toArray()[position]);
		txtTitle.setText((String)web.toArray()[position]);
        txtTitle.setTypeface(textTypeface);
        txtTitle.setTextSize(textSize);
		imageView.setImageResource((Integer)imageId.toArray()[position]);
		return rowView;
	}
	
	
	
}
