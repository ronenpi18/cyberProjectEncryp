package project.unitato.encrypchat;

import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChatList extends ArrayAdapter<String>{
	private final Activity context;
	private final ArrayList<String> web;
	private final ArrayList<Integer> imageId;
    private final ArrayList<String> times;
    private final ArrayList<Integer> userId;
    private Typeface textTypeface;
    private int textSize;
    private int drawable1=-1, drawable2=-1;
	
	public ChatList(Activity context, ArrayList<String> web, ArrayList<Integer> imageId, ArrayList<String> times, Typeface font, int textSize) {
        super(context, R.layout.chat_single, web);
        this.context = context;
        this.textTypeface = font;
        this.textSize= textSize;
        this.web = web;
        this.imageId = imageId;
        this.times = times;
        this.userId = new ArrayList<Integer>();
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
        int currentImg = (Integer) imageId.toArray()[position];
        RelativeLayout layout = (RelativeLayout) rowView.findViewById(R.id.chat_single_layout);
        if(drawable1 == -1)
            drawable1 = currentImg;
        else if(drawable2 == -1 && currentImg != drawable1) {
            drawable2 = currentImg;
            layout.setBackgroundColor(Color.rgb(235, 234, 254));
        }else if(currentImg == drawable2)
            layout.setBackgroundColor(Color.rgb(235, 234, 254));
        if(position > 0 && (Integer) imageId.toArray()[position-1] == currentImg) {
           // times.set(position - 1, "");
        }
        timeTv.setText((String) times.toArray()[position]);
		txtTitle.setText((String)web.toArray()[position]);
        txtTitle.setTypeface(textTypeface);
        txtTitle.setTextSize(25);
		imageView.setImageResource(currentImg);
		return rowView;
	}
	
	
	
}
