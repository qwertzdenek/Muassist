step=1024;
for i = 0:10
  amdfanl(snd(i*step+1:(i+1)*step))
  pause
endfor
