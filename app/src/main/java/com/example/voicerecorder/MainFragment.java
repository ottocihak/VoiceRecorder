package com.example.voicerecorder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.voicerecorder.databinding.FragmentMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;

    private ImageButton stateBtn;
    private String state;
    private FloatingActionButton recBtn;
    private FloatingActionButton stopBtn;
    private FloatingActionButton playBtn;
    private MediaRecorder mediaRecorder;
    private MediaPlayer player;
    private File fileName;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentMainBinding.inflate(inflater, container, false);

        ActivityCompat.requestPermissions(requireActivity(), permissions,
                REQUEST_RECORD_AUDIO_PERMISSION);

        stateBtn = binding.stateButton;
        recBtn = binding.recBtn;
        stopBtn = binding.stopBtn;
        playBtn = binding.playBtn;

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View.OnClickListener listener = new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.recBtn:
                        updateView("rec");
                        if (mediaRecorder == null) {
                            mediaRecorder = new MediaRecorder();
                            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                            newFile();
                            mediaRecorder.setOutputFile(fileName);
                            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                            try {
                                mediaRecorder.prepare();
                            } catch (IOException e) {
                                Log.e("onClick: ","not able to record");
                            }
                            mediaRecorder.start();
                        }
                        break;
                    case R.id.stopBtn:
                        updateView("stop");
                        if (mediaRecorder != null){
                            mediaRecorder.stop();
                            mediaRecorder.reset();
                            mediaRecorder.release();
                            mediaRecorder = null;
                        } else if (player != null) {
                            player.stop();
                            player.release();
                            player = null;
                        }
                        break;
                    case R.id.playBtn:
                        updateView("play");
                        if (mediaRecorder == null && player == null) {
                            player = new MediaPlayer();

                            try {
                                player.setDataSource(String.valueOf(fileName));
                                player.prepare();
                                player.start();

                                player.setOnCompletionListener(mediaPlayer -> {
                                    stopBtn.callOnClick();
                                });

                                }catch (Exception e){
                                Log.e("onClick: ", "not able to play media");
                            }
                        }
                        break;
                    case R.id.stateButton:
                        if (state.equals("stop")) {
                            recBtn.callOnClick();
                        } else if (state.equals("rec")) {
                            stopBtn.callOnClick();
                        }
                        break;
                }
            }
        };

        recBtn.setOnClickListener(listener);
        playBtn.setOnClickListener(listener);
        stopBtn.setOnClickListener(listener);
    }

    private void newFile () {
        String path = getContext().getExternalFilesDir(null).getAbsolutePath();
        fileName = new File(path +
                "/"+new SimpleDateFormat("dd/MM/yyyy_HH:mm").format(System.currentTimeMillis())+".3gp");

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateView(String state) {
        switch (state){
            case "rec":
                stateBtn.setImageDrawable
                        (getResources().getDrawable(R.drawable.ic_baseline_record_voice_over_24));
                break;
            case "stop":
                stateBtn.setImageDrawable
                        (getResources().getDrawable(R.drawable.ic_baseline_mic_24));
                stateBtn.setImageTintList(getResources().getColorStateList(R.color.blue));
                break;
            case "play":
                stateBtn.setImageDrawable
                        (getResources().getDrawable(R.drawable.ic_baseline_play_circle_24));
                stateBtn.setImageTintList(getResources().getColorStateList(R.color.green));
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED;
        }
        if (!permissionToRecordAccepted) {
            Toast.makeText(
                    getContext(),
                    "Permission required",
                    Toast.LENGTH_LONG
            ).show();
            requireActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}