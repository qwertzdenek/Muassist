function retval = amdfanl(wave, rate)
mini=16
maxi=400

if (nargin < 2)
  disp('volejte amdfanl(wave, rate)')
endif

resAMDF = zeros(length(wave),1);

for m = 1:length(wave)
  n = 1:(length(wave) - m + 1);
  s = sum(abs(wave(n + m - 1) - wave(n)));
  resAMDF(m) = s / (length(wave) - m);
endfor

level=0.42 * (max(resAMDF) + min(resAMDF));

resClip=zeros(maxi-mini,1);
for i = mini:maxi
  if (resAMDF(i) < level)
    resClip(i - mini + 1) = 1;
  else
    resClip(i - mini + 1) = 0;
  endif
endfor

resACF = autocor(resClip);

plot(resACF)

% peaks = find([a(2:n,1) - a(1:n-1,1) < 0; 1] & [1; a(1:n-1,1) - a(2:n,1) < 0]);
% distances
% retval = (averange distance) * rate

endfunction
