package kiv.janecekz.ma.tuner;

public class LeastSquares {
    public static double[][] getMatrix(double[] points, int count) {
        double[][] matrix = new double[3][3];

        // sum x[i]
        double pow1 = 0.0;
        double pow2 = 0.0;
        double pow3 = 0.0;
        double pow4 = 0.0;
        
        double buf2;
        double buf3;
        
        for (int i = 0; i < count; i++) {
            pow1 += i;
            
            buf2 =  i * i;
            pow2 += buf2;
            
            buf3 =  buf2 * i;
            pow3 += buf3;
            
            pow4 += buf3 * i;
        }
        
        matrix[0][0] = count + 1;
        matrix[0][1] = pow1;
        matrix[0][2] = pow2;
        matrix[1][0] = pow1;
        matrix[1][1] = pow2;
        matrix[1][2] = pow3;
        matrix[2][0] = pow2;
        matrix[2][1] = pow3;
        matrix[2][2] = pow4;
        
        return matrix;
    }
    
    public static double[] getB(double[] points, int count) {
        double[] b = new double[3];
        
        for (int i = 0; i < count; i++) {
            b[0] += points[i];
            b[1] += points[i] * i;
            b[2] += points[i] * i * i;
        }
        
        return b;
    }
    
    /**
     * Computes determinant by substituting i-th column by vector v. 
     * @param m used matrix
     * @param v vector
     * @param i column number
     * @return Determinant of the matrix
     */
    private static double det(double[][] m, double[] v, int i) {
        double x = 0.0, y = 0.0;
        
        if (v == null) {
            x = m[0][0] * m[1][1] * m[2][2] + m[1][0] * m[2][1] * m[0][2] + m[2][0] * m[0][1] * m[1][2];
            y = m[0][2] * m[1][1] * m[2][0] + m[1][2] * m[2][1] * m[0][0] + m[2][2] * m[0][1] * m[1][0];
        } else {
            switch (i) {
            case 0:
                x = v[0] * m[1][1] * m[2][2] + v[1] * m[2][1] * m[0][2] + v[2] * m[0][1] * m[1][2];
                y = m[0][2] * m[1][1] * v[2] + m[1][2] * m[2][1] * v[0] + m[2][2] * m[0][1] * v[1];
                break;
            case 1:
                x = m[0][0] * v[1] * m[2][2] + m[1][0] * v[2] * m[0][2] + m[2][0] * v[0] * m[1][2];
                y = m[0][2] * v[1] * m[2][0] + m[1][2] * v[2] * m[0][0] + m[2][2] * v[0] * m[1][0];
                break;
            case 2:
            	x = m[0][0] * m[1][1] * v[2] + m[1][0] * m[2][1] * v[0] + m[2][0] * m[0][1] * v[1];
                y = v[0] * m[1][1] * m[2][0] + v[1] * m[2][1] * m[0][0] + v[2] * m[0][1] * m[1][0];
                break;
            default:
                break;
            }
        }
        
        return x - y;
    }
    
    /**
     * Solves equation Ax=b. {c0, c1, c2}
     * @param A 3x3 matrix
     * @param b result
     * @return solution x
     */
    public static double[] solve(double[][] A, double[] b) {
        double detA = det(A, null, 0);
        double det0 = det(A, b, 0);
        double det1 = det(A, b, 1);
        double det2 = det(A, b, 2);
        
        return new double[] {det0 / detA, det1 / detA, det2 / detA};
    }
}
