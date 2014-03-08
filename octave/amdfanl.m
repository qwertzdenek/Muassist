function freq = amdfanl(wave)

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



            for (int k = 0; k < resACF.length; k++) {
                sum = 0;
                for (int n = 0; n < resClip.length - k; n++) {
                    sum += resClip[n] && resClip[n + k] ? 1 : 0;
                }
                resACF[k] = (double) sum / (resClip.length - k);
            }

% retval = rate / (averange distance)

endfunction
