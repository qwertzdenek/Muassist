function retval = amdfanl(wave, rate)
pkg load signal

mini=16;
maxi=400;

resAMDF = zeros(length(wave),1);

for m = 1:length(wave)
  n = 1:(length(wave) - m + 1);
  s = sum(abs(wave(n + m - 1) - wave(n)));
  resAMDF(m) = s / (length(wave) - m + 1);
endfor

level=0.43 * (max(resAMDF(1:750,1)) + min(resAMDF(1:750,1)));

resClip=zeros(maxi-mini,1);
for i = mini:maxi
  if (resAMDF(i) < level)
    resClip(i - mini + 1) = 1;
  else
    resClip(i - mini + 1) = 0;
  endif
endfor

a = xcorr(resClip);
n = length(a)

peaks = find([a(2:n,1) - a(1:n-1,1) < 0; 1] & [1; a(1:n-1,1) - a(2:n,1) < 0])
% distances
% retval = (averange distance) * rate

endfunction
