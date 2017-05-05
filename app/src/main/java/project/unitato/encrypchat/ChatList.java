package project.unitato.encrypchat;

import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    /**
     * Constructor of Chat list
     * @param context
     * @param web
     * @param imageId
     * @param times
     * @param font
     * @param textSize
     */
	public ChatList(Activity context, ArrayList<String> web, ArrayList<Integer> imageId, ArrayList<String> times, Typeface font, int textSize) {
        super(context, R.layout.li_msg, web);
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

    /**
     *
     * @param position
     * @param view
     * @param parent
     * @return
     */
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView= inflater.inflate(R.layout.li_msg, null, true);
		TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
//        TextView timeTv = (TextView) rowView.findViewById(R.id.time_tv);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        int currentImg = (Integer) imageId.toArray()[position];
        RelativeLayout layout = (RelativeLayout) rowView.findViewById(R.id.chat_single_layout);
        if(drawable1 == -1)
            drawable1 = currentImg;
        else if(drawable2 == -1 && currentImg != drawable1) {
            drawable2 = currentImg;
//            layout.setBackgroundColor(Color.rgb(235, 234, 254));
            txtTitle.setBackgroundResource(R.drawable.chat_bubble_right);
            LinearLayout rootLayout = (LinearLayout) rowView.findViewById(R.id.linear_layout);
            View imgContainer = rowView.findViewById(R.id.pp_container);
            imgContainer.setBackgroundResource(R.drawable.pp_background_round);
            rootLayout.removeView(txtTitle);
            rootLayout.addView(txtTitle);
            rootLayout.setGravity(Gravity.START);
//            imageView.setImageDrawable(selfDrawable);
        }else if(currentImg == drawable2) {
//            layout.setBackgroundColor(Color.rgb(235, 234, 254));
            txtTitle.setBackgroundResource(R.drawable.chat_bubble_left);
            LinearLayout rootLayout = (LinearLayout) rowView.findViewById(R.id.linear_layout);
            View imgContainer = rowView.findViewById(R.id.pp_container);
            imgContainer.setBackgroundResource(R.drawable.pp_background_round_2);
            rootLayout.removeView(imgContainer);
            rootLayout.addView(imgContainer);
            rootLayout.setGravity(Gravity.END);
//            ppImg.setImageDrawable(otherDrawable);
        }
//        timeTv.setText((String) times.toArray()[position]);
        if(times.toArray()[position].equals(""))
//            timeTv.setVisibility(View.GONE);
        txtTitle.setTypeface(textTypeface);
        String message = (String) web.toArray()[position];
        txtTitle.setText(Html.fromHtml(message));
        txtTitle.setMovementMethod(LinkMovementMethod.getInstance());
		imageView.setImageResource(currentImg);
        if(position > 0 && (Integer) imageId.toArray()[position-1] == currentImg) {
//            imageView.setVisibility(View.GONE);

        }
        if(position < times.size() - 1 && (Integer) imageId.toArray()[position+1] == currentImg && times.get(position+1).substring(0,2).equals(times.get(position).substring(0,2)) && Integer.parseInt(times.get(position+1).substring(3,5)) - Integer.parseInt(times.get(position).substring(3,5)) < 15){
//            timeTv.setVisibility(View.GONE);
        }


        Typeface robotoCondensed = Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed-Regular.ttf");
        txtTitle.setTypeface(robotoCondensed);
		return rowView;
	}




    String replaceCoorelation(String msg, String newWord)
    {
        for(int i = 0; i < msg.length(); i++)
        {
            int j;
            for(j = i; j < msg.length() && msg.charAt(j) != ' '; j++);
            String word = msg.substring(i, j);
            int coorelations = 0;
            for(int x = 0; x < word.length() && x < newWord.length(); x++)
            {
                if(word.charAt(x) != newWord.charAt(x))
                    coorelations++;
            }
            if(coorelations == 1)
            {
                msg = msg.replace(word, newWord);
            }
            i  = j;
        }
        return  msg;
    }
	
	
	
}
