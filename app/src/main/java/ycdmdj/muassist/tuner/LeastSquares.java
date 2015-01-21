/*
Musicians Assistant
    Copyright (C) 2014  Zdeněk Janeček <jan.zdenek@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ycdmdj.muassist.tuner;

public class LeastSquares {
    public static int[][] getMatrix(int start, int count) {
        int[][] matrix = new int[2][2];

        // sum x[i]
        int pow1 = 0;
        int pow2 = 0;

        for (int i = start; i < start + count; i++) {
            pow1 += i;
            pow2 += i * i;
        }

        matrix[0][0] = count;
        matrix[0][1] = pow1;
        matrix[1][0] = pow1;
        matrix[1][1] = pow2;

        return matrix;
    }

    public static double[] getB(int start, double[] points, int count) {
        double[] b = new double[2];

        for (int i = 0; i < count; i++) {
            b[0] += points[i];
            b[1] += points[i] * (i + start);
        }

        return b;
    }

    /**
     * Computes determinant 2x2 by substituting i-th column by vector v.
     *
     * @param m used matrix
     * @param v vector
     * @param i column number
     * @return Determinant of the matrix
     */
    private static double det(int[][] m, double[] v, int i) {
        double x = 0, y = 0;

        if (v == null) {
            x = m[0][0] * m[1][1];
            y = m[0][1] * m[1][0];
        } else {
            switch (i) {
                case 0:
                    x = v[0] * m[1][1];
                    y = m[0][1] * v[1];
                    break;
                case 1:
                    x = m[0][0] * v[1];
                    y = v[0] * m[1][0];
                    break;
                default:
                    break;
            }
        }

        return x - y;
    }

    /**
     * Solves equation Ax=b. c0 + c1 * x
     *
     * @param A 2x2 matrix
     * @param b result
     * @return solution x
     */
    public static double[] solve(int[][] A, double[] b) {
        double detA = det(A, null, 0);
        double det0 = det(A, b, 0);
        double det1 = det(A, b, 1);

        return new double[]{det0 / detA, det1 / detA};
    }
}
