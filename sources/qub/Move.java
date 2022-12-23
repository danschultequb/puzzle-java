package qub;

public class Move
{
    private static final String startLocationPropertyName = "startLocation";
    private static final String endLocationProperteyName = "endLocation";
    private static final String blockBrokenPropertyName = "blockBroken";
    private static final String endLocationIsGoalPropertyName = "endLocationIsGoal";

    private Point2Integer startLocation;
    private Point2Integer endLocation;
    private Boolean blockBroken;
    private Boolean endLocationIsGoal;

    private Move()
    {
    }

    public static Move create()
    {
        return new Move();
    }

    public Move setStartLocation(Point2Integer startLocation)
    {
        PreCondition.assertNotNull(startLocation, "startLocation");

        this.startLocation = startLocation;

        return this;
    }

    public Point2Integer getStartLocation()
    {
        PreCondition.assertNotNull(this.startLocation, "this.startLocation");

        return this.startLocation;
    }

    public Move setEndLocation(Point2Integer endLocation)
    {
        PreCondition.assertNotNull(endLocation, "endLocation");

        this.endLocation = endLocation;

        return this;
    }

    public Point2Integer getEndLocation()
    {
        PreCondition.assertNotNull(this.endLocation, "this.endLocation");

        return this.endLocation;
    }

    public Move setBlockBroken(boolean blockBroken)
    {
        this.blockBroken = blockBroken;

        return this;
    }

    public boolean getBlockBroken()
    {
        return Booleans.isTrue(this.blockBroken);
    }

    public Point2Integer getBrokenBlockLocation()
    {
        PreCondition.assertTrue(this.getBlockBroken(), "this.getBlockBroken()");

        Point2Integer result;

        final int xDifference = this.endLocation.getXAsInt() - this.startLocation.getXAsInt();
        if (xDifference < 0)
        {
            result = Point2Integer.create(this.endLocation.getXAsInt() - 1, this.endLocation.getYAsInt());
        }
        else if (xDifference > 0)
        {
            result = Point2Integer.create(this.endLocation.getXAsInt() + 1, this.endLocation.getYAsInt());
        }
        else
        {
            final int yDifference = this.endLocation.getYAsInt() - this.startLocation.getYAsInt();
            if (yDifference < 0)
            {
                result = Point2Integer.create(this.endLocation.getXAsInt(), this.endLocation.getYAsInt() - 1);
            }
            else
            {
                result = Point2Integer.create(this.endLocation.getXAsInt(), this.endLocation.getYAsInt() + 1);
            }
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public Move setEndLocationIsGoal(boolean endLocationIsGoal)
    {
        this.endLocationIsGoal = endLocationIsGoal;

        return this;
    }

    public boolean getEndLocationIsGoal()
    {
        return Booleans.isTrue(this.endLocationIsGoal);
    }

    public JSONObject toJson()
    {
        final JSONObject result = JSONObject.create();

        if (this.startLocation != null)
        {
            result.setString(Move.startLocationPropertyName, this.startLocation.toString());
        }

        if (this.endLocation != null)
        {
            result.setString(Move.endLocationProperteyName, this.endLocation.toString());
        }

        if (this.blockBroken != null)
        {
            result.setBoolean(Move.blockBrokenPropertyName, this.blockBroken);
        }

        if (this.endLocationIsGoal != null)
        {
            result.setBoolean(Move.endLocationIsGoalPropertyName, this.endLocationIsGoal);
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    @Override
    public String toString()
    {
        return this.toJson().toString();
    }

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof Move && this.equals((Move)rhs);
    }

    public boolean equals(Move rhs)
    {
        return rhs != null &&
            Comparer.equal(this.startLocation, rhs.startLocation) &&
            Comparer.equal(this.endLocation, rhs.endLocation) &&
            Comparer.equal(this.getBlockBroken(), rhs.getBlockBroken()) &&
            Comparer.equal(this.getEndLocationIsGoal(), rhs.getEndLocationIsGoal());
    }
}
