package de.ggj14bremen.withoutplan.view.framents;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import de.ggj14bremen.withoutplan.R;
import de.ggj14bremen.withoutplan.controller.TimeScoreInfo;

public class GameFragment extends BaseFragment implements OnClickListener
{
	private Button btnPause;
	private TextView infoTextView;
	private CountDownTimer timer;
	private TextView textViewCountdown;
	private TextView tvRedScore, tvGreenScore, tvBlueScore;
	private List<String> infoTextList = new ArrayList<String>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_game, container, false);
		v.findViewById(R.id.buttonReset).setOnClickListener(this);
		btnPause 		= (Button) v.findViewById(R.id.buttonPause);
		btnPause.setOnClickListener(this);
		infoTextView 	= (TextView) v.findViewById(R.id.infoTextView);
		tvRedScore 		= (TextView) v.findViewById(R.id.textViewScoreRed);
		tvGreenScore 	= (TextView) v.findViewById(R.id.textViewScoreGreen);
		tvBlueScore 	= (TextView) v.findViewById(R.id.textViewScoreBlue);
		textViewCountdown = (TextView) v.findViewById(R.id.textViewCountdown);

		return v;
	}

	@Override
	public void onStart()
	{
		super.onStart();
		startTimer();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		timer.cancel();
	}

	private void startTimer()
	{
		long time = 21000;
		timer = new CountDownTimer(time, 500)
		{
			public void onTick(long millisUntilFinished)
			{
				TimeScoreInfo scoreInfo = activity.gameThread.getTimeScoreInfo();
				if(activity.gameThread.isPaused())
				{
					btnPause.setText("Play");
				}
				else
				{
					btnPause.setText("Pause");
				}
				if (activity.gameThread.getTimeScoreInfo().isTimeShowed())
				{
					final long secondsRemaining = activity.gameThread.getTimeScoreInfo().getStepTime() / 1000;
					textViewCountdown.setText(String.valueOf(secondsRemaining));
					if(activity.gameThread.getTimeScoreInfo().getCurrentColor() != null)
						textViewCountdown.setTextColor(activity.gameThread.getTimeScoreInfo().getCurrentColor().getColorAsInt());				
				}
				else
				{
					textViewCountdown.setText("");
				}
				if(scoreInfo.isGameEnded()) 
				{
					activity.showSplashScreen(true, "Score: "+scoreInfo.getScore()+"   Round: "+scoreInfo.getRound());
					scoreInfo.setGameEnded(false);
				}
				infoTextView.setText(activity.gameThread.getTimeScoreInfo().getInfoText());
				tvBlueScore.setText(activity.gameThread.getTimeScoreInfo().getBlueScore()+"");
				tvRedScore.setText(activity.gameThread.getTimeScoreInfo().getRedScore()+"");
				tvGreenScore.setText(activity.gameThread.getTimeScoreInfo().getGreenScore()+"");
			}
			public void onFinish()
			{
				startTimer();
			}
		}.start();
	}

	@Override
	public void onClick(View v)
	{
		if (v.getId() == R.id.buttonReset)
		{
			activity.gameThread.reset();
			infoTextList.clear();
		} 
		else if (v.getId() == R.id.buttonPause)
		{
			if(!activity.gameThread.isRunning())
			{
				activity.gameThread.start();
				activity.gameThread.setPause(false);
			}
			else activity.gameThread.togglePause();
		}
	}
}
