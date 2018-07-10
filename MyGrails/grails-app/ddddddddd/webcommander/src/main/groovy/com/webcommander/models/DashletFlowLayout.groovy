package com.webcommander.models

import com.webcommander.admin.Dashlet

/**
 * Created by zobair on 29/12/2014.
 */
class DashletFlowLayout {
    List points = [[0, 0f], [0, 0f], [0, 101f], [0, 101f]]
    public int highestPoint = 0

    public Float findGap(int required) {
        List possibles = []
        for(int pointIndex = 0; pointIndex < points.size()/2 - 1; pointIndex++) {
            if(highestPoint != points[pointIndex*2 + 1][0] && points[(pointIndex+1)*2 + 1][1] - points[pointIndex*2 + 1][1] >= required) {
                possibles.add points[pointIndex*2 + 1]
            }
        }
        possibles.min { a, b ->
            a[0] <=> b[0]
        }?.getAt(1)
    }

    public DashletFlow addAt(float position, Dashlet dashlet) {
        DashletFlow flow = new DashletFlow(dashlet: dashlet)
        for(int pointIndex = 0; pointIndex < points.size()/2 - 1; pointIndex++) {
            if(points[pointIndex*2 + 1][1] == position) {
                flow.left = position;
                flow.top = points[pointIndex*2 + 1][0]
                points[pointIndex*2 + 1][0] += dashlet.height + 1
                if(points[pointIndex*2 + 1][0] > highestPoint) {
                    highestPoint = points[pointIndex*2 + 1][0]
                }
                float referenceWidth = points[pointIndex*2][1]
                if(points[pointIndex*2 + 1][0] == points[pointIndex*2][0]) {
                    points.remove(pointIndex*2)
                    points.remove(pointIndex*2)
                    pointIndex--
                }
                if(points[(pointIndex + 1)*2][1] == points[pointIndex*2][1] + dashlet.width + 1) {
                    points[(pointIndex + 1)*2][0] += dashlet.height + 1
                    if(points[(pointIndex + 1)*2][0] > highestPoint) {
                        highestPoint = points[(pointIndex + 1)*2][0]
                    }
                    if(points[(pointIndex + 1)*2 + 1][0] == points[(pointIndex + 1)*2][0]) {
                        points.remove((pointIndex + 1)*2)
                        points.remove((pointIndex + 1)*2)
                    }
                } else {
                    int nowHeight = points[(pointIndex + 1)*2][0]
                    int newHeight = nowHeight + dashlet.height + 1
                    float newWidth = referenceWidth + dashlet.width + 1
                    points.add((pointIndex + 1)*2, [nowHeight, newWidth])
                    points.add((pointIndex + 1)*2, [newHeight, newWidth]) //1 is margin
                    if(newHeight > highestPoint) {
                        highestPoint = newHeight
                    }
                }
                break;
            }
        }
        return flow
    }
}
