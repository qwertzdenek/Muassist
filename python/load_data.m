function [P] = load_data(f)

%otevreni souboru 
fid = fopen(f, 'r');

line = fgetl(fid);
P = zeros(str2num(line), 2);

i = 1;
%nacteni modelu
while ~feof(fid)
  line = fgetl(fid);
  P(i,:) = str2num(line);
  i += 1;

endwhile
end
