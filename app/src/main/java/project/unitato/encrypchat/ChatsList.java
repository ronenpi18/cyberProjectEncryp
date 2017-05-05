package project.unitato.encrypchat;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Typeface;
import android.text.Html;
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

	/**
	 * constructor of chats list
	 * @param context
	 * @param web
	 * @param imageId
	 * @param lastMsgs
	 * @param font
	 * @param textSize
	 */
	public ChatsList(Activity context, ArrayList<String> web, ArrayList<Integer> imageId, ArrayList<String> lastMsgs, Typeface font, int textSize) {
			super(context, R.layout.chats_single, web);
			this.context = context;
            this.textTypeface = font;
            this.textSize= textSize;
			this.web = web;
			this.imageId = imageId;
            this.lastMsgs = lastMsgs;
			}


	/**
	 * void function, which adds to chatlist
	 * @param object
	 */
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

	/**
	 * gets the view and finds the need of bolding the text
	 * @param position
	 * @param view
	 * @param parent
	 * @return rowView- inflater..
	 */
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView= inflater.inflate(R.layout.chats_single, null, true);
		TextView txtTitle = (TextView) rowView.findViewById(R.id.name_tv);
        TextView lastMsgTv = (TextView) rowView.findViewById(R.id.lastmsg_tv);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        String message =  (String) lastMsgs.toArray()[position];
        boolean encountered = false;
        for(int i = 0; i < message.length(); i++)
            if(message.charAt(i) == '*') //Bold text - to make text bold, we need to set finder of *
            {
                if(encountered)
                    message = message.replaceFirst("\\*", "</b>");
                else
                    message = message.replaceFirst("\\*", "<b>");
                encountered = !encountered;
            }
        lastMsgTv.setText(Html.fromHtml(message));
		txtTitle.setText((String)web.toArray()[position]);
        txtTitle.setTypeface(textTypeface);
        txtTitle.setTextSize(textSize);
		imageView.setImageResource((Integer)imageId.toArray()[position]);
		return rowView;
	}
	
	
	
}
