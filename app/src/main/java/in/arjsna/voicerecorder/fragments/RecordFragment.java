package in.arjsna.voicerecorder.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import com.melnykov.fab.FloatingActionButton;
import in.arjsna.voicerecorder.R;
import in.arjsna.voicerecorder.audiovisualization.AudioVisualization;
import in.arjsna.voicerecorder.recording.AudioRecordService;
import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link RecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordFragment extends Fragment {
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_POSITION = "position";
  private static final String LOG_TAG = RecordFragment.class.getSimpleName();

  private int position;

  //Recording controls
  private FloatingActionButton mRecordButton = null;
  private Button mPauseButton = null;
  private AudioVisualization audioVisualization;

  private boolean mIsRecording = false;
  private boolean mPauseRecording = true;

  long timeWhenPaused = 0; //stores time when user clicks pause button

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment Record_Fragment.
   */
  public static RecordFragment newInstance(int position) {
    RecordFragment f = new RecordFragment();
    Bundle b = new Bundle();
    b.putInt(ARG_POSITION, position);
    f.setArguments(b);

    return f;
  }

  public RecordFragment() {
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    position = getArguments().getInt(ARG_POSITION);
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View recordView = inflater.inflate(R.layout.fragment_record, container, false);
    initViews(recordView);
    bindEvents();
    return recordView;
  }

  private void bindEvents() {
    mRecordButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        onChangeRecord();
      }
    });

    mPauseButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        onPauseRecord(mPauseRecording);
        mPauseRecording = !mPauseRecording;
      }
    });
  }

  private void initViews(View recordView) {
    audioVisualization = (AudioVisualization) recordView.findViewById(R.id.visualizer_view);

    //update recording prompt text

    mRecordButton = (FloatingActionButton) recordView.findViewById(R.id.btnRecord);
    mRecordButton.setImageResource(
        mIsRecording ? R.drawable.ic_media_stop : R.drawable.ic_media_play);
    mRecordButton.setColorNormal(getResources().getColor(R.color.primary));
    mRecordButton.setColorPressed(getResources().getColor(R.color.primary_dark));
    mPauseButton = (Button) recordView.findViewById(R.id.btnPause);
    mPauseButton.setVisibility(View.GONE); //hide pause button before recording starts
  }

  // Recording Start/Stop
  //TODO: recording pause
  private void onChangeRecord() {

    Intent intent = new Intent(getActivity(), AudioRecordService.class);

    if (!mIsRecording) {
      // start recording
      mIsRecording = true;
      mRecordButton.setImageResource(R.drawable.ic_media_stop);
      //mPauseButton.setVisibility(View.VISIBLE);
      Toast.makeText(getActivity(), R.string.toast_recording_start, Toast.LENGTH_SHORT).show();
      File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
      if (!folder.exists()) {
        //folder /SoundRecorder doesn't exist, create the folder
        folder.mkdir();
      }

      //start RecordingService
      getActivity().startService(intent);
      //keep screen on while recording
      getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    } else {
      mIsRecording = false;
      mRecordButton.setImageResource(R.drawable.ic_media_play);
      getActivity().unbindService(serviceConnection);
      getActivity().stopService(intent);
      //allow the screen to turn off again once recording is finished
      getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
  }

  ServiceConnection serviceConnection = new ServiceConnection() {
    @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
      AudioRecordService audioRecordService =
          ((AudioRecordService.ServiceBinder) iBinder).getService();
      audioVisualization.linkTo(audioRecordService.getHandler());
      Log.i("Tesing", " " + audioRecordService.isRecording() + " recording");
      mIsRecording = audioRecordService.isRecording();
      if (mIsRecording) {
        mRecordButton.setImageResource(R.drawable.ic_media_stop);
      }
    }

    @Override public void onServiceDisconnected(ComponentName componentName) {
      audioVisualization.release();
    }
  };

  //TODO: implement pause recording
  private void onPauseRecord(boolean pause) {
    if (pause) {
      //pause recording
      mPauseButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_play, 0, 0, 0);
    } else {
      //resume recording
      mPauseButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_pause, 0, 0, 0);
    }
  }
}