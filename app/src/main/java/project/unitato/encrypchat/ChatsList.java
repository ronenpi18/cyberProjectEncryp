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

public class ChatsList extends ArrayAdapter<String>{
	private final Activity context;
	private final ArrayList<String> web;
    private final ArrayList<String> lastMsgs;
	private final ArrayList<Integer> imageId;
    private Typeface textTypeface;
    private int textSize;
	
	public ChatsList(Activity context, ArrayList<String> web, ArrayList<Integer> imageId, ArrayList<String> lastMsgs, Typeface font, int textSize) {
			super(context, R.layout.chats_single, web);
			this.context = context;
            this.textTypeface = font;
            this.textSize= textSize;
			this.web = web;
			this.imageId = imageId;
            this.lastMsgs = lastMsgs;
			}
	
	
	
	
	@Override
	public void add(String object) {
		this.web.add(object);
		this.imageId.add((Integer)imageId.toArray()[0]);
		notifyDataSetChanged();
	}
	
	public void postMessage(int imageResId, String message)
	{
		this.web.add(message);
        this.imageId.add(imageResId);
        notifyDataSetChanged();
	}




	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView= inflater.inflate(R.layout.chats_single, null, true);
		TextView txtTitle = (TextView) rowView.findViewById(R.id.name_tv);
        TextView lastMsgTv = (TextView) rowView.findViewById(R.id.lastmsg_tv);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        lastMsgTv.setText((String) lastMsgs.toArray()[position]);
		txtTitle.setText((String)web.toArray()[position]);
        txtTitle.setTypeface(textTypeface);
        txtTitle.setTextSize(textSize);
		imageView.setImageResource((Integer)imageId.toArray()[position]);
		return rowView;
	}
	
	
	
}
