function freq = amdfanl(wave)
graphics_toolkit('gnuplot')
pkg load signal

N = length(wave) / 2;
resAMDF = zeros(N, 1);

%~ for m = 1 : N
  %~ n = N + 1 : 2 * N - m;
  %~ s = sum(abs(wave(n + m) - wave(n)));
  %~ resAMDF(m) = s / (length(wave) - m + 1);
%~ endfor
%~ 
%~ plot(resAMDF)

%~ pause
%moddified
for m = 1 : N
  n = 2 * N : -1 : N + 1;
  s = sum(abs(wave(n - m) - wave(n)));
  resAMDF(m) = s / (N - 1);
endfor

level=0.4 * (max(resAMDF) + min(resAMDF));

resClip=zeros(N - 10, 1);
for i = 1 : N - 10
  if (resAMDF(i) < level)
    resClip(i) = 1;
  else
    resClip(i) = 0;
  endif
endfor

y = xcorr(resClip, 'unbiased');
x = -(length(resClip) - 1):(length(resClip) - 1);

[pks, loc] = findpeaks (abs(y));

plot(x, y, x(loc), y(loc), 'om')
x(loc)

dist=loc(2:length(loc)) - loc(1:length(loc) - 1)

% retval = rate / (averange distance)

endfunction
