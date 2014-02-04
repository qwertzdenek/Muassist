function retval = amdfanl(wave)
graphics_toolkit('gnuplot')
pkg load signal

mini=16;
maxi=400;

N = length(wave) / 2;
resAMDF = zeros(N, 1);

for m = 1 : N
  n = N + 1 : 2 * N - m;
  s = sum(abs(wave(n + m) - wave(n)));
  resAMDF(m) = s / (length(wave) - m + 1);
endfor

plot(resAMDF)

pause
%moddified
for m = 1 : N
  n = 2 * N : -1 : N + 1;
  s = sum(abs(wave(n - m) - wave(n)));
  resAMDF(m) = s / (N - 1);
endfor

plot(resAMDF)

pause

level=0.4 * (max(resAMDF) + min(resAMDF));

resClip=zeros(maxi-mini,1);
for i = mini:maxi
  if (resAMDF(i) < level)
    resClip(i - mini + 1) = 1;
  else
    resClip(i - mini + 1) = 0;
  endif
endfor

y = xcorr(resClip, 'unbiased');
x = -(length(resClip) - 1):(length(resClip) - 1);

[pks, loc] = findpeaks (abs(y));

plot(x, y, x(loc), y(loc), 'om')

% distances
% retval = rate / (averange distance)

endfunction
