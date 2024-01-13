# data.dat
# Kanalverlust Wahrscheinlichkeit
0.01 0.0199
0.02 0.0396
0.03 0.0589
0.04 0.0776
0.05 0.0956
# Füge weitere Zeilen hinzu, wenn nötig

# Gnuplot Skript
set terminal pngcairo enhanced font 'arial,10' size 800, 600
set output 'verlustwahrscheinlichkeit.png'

set title 'Verlustwahrscheinlichkeit eines Bildes in Abhängigkeit von der Kanalverlustrate'
set xlabel 'Kanalverlustrate'
set ylabel 'Verlustwahrscheinlichkeit'

plot 'data.dat' using 1:2 with linespoints title 'n=2', \
     'data.dat' using 1:(1-(1-$1)**5) with linespoints title 'n=5'

set output