package qub;

/**
 * A room that a puzzle can take place in.
 */
public class Room
{
    private final MutableMap<Point2Integer,RoomObject> locationToRoomObjectMap;

    private Room()
    {
        this.locationToRoomObjectMap = Map.create();
    }

    /**
     * Create a new {@link Room}.
     */
    public static Room create()
    {
        return new Room();
    }

    /**
     * Get the number of {@link RoomObject}s that exist in this {@link Room}.
     */
    public int getRoomObjectCount()
    {
        return this.locationToRoomObjectMap.getCount();
    }

    /**
     * Get the number of the provided {@link RoomObject} that exist in this {@link Room}.
     */
    public int getRoomObjectCount(RoomObject roomObject)
    {
        return this.locationToRoomObjectMap.iterateValues()
            .where((RoomObject existingObject) -> existingObject == roomObject)
            .getCount();
    }

    /**
     * Get the {@link RoomObject} at the provided location.
     * @param location The {@link Point2Integer} location to look up.
     */
    public Result<RoomObject> getRoomObject(Point2Integer location)
    {
        PreCondition.assertNotNull(location, "location");

        return this.locationToRoomObjectMap.get(location);
    }

    /**
     * Get whether a {@link RoomObject} exists at the provided location.
     * @param location The location to look at.
     */
    public boolean isLocationOccupied(Point2Integer location)
    {
        return this.getRoomObject(location)
            .then(() -> true)
            .catchError(NotFoundException.class, () -> false)
            .await();
    }

    /**
     * Get an {@link Iterator} that returns the locations of the provided {@link RoomObject}.
     * @param roomObject The {@link RoomObject} to return the locations of.
     */
    public Iterator<Point2Integer> iterateRoomObjectLocations(RoomObject roomObject)
    {
        PreCondition.assertNotNull(roomObject, "roomObject");

        return this.locationToRoomObjectMap.iterate()
            .where((MapEntry<Point2Integer,RoomObject> entry) -> entry.getValue() == roomObject)
            .map(MapEntry::getKey);
    }

    /**
     * Add the provided {@link RoomObject} at the provided location.
     * @param roomObject The {@link RoomObject} to add at the provided location.
     * @param location The location to add the {@link RoomObject} at.
     * @return This object for method chaining.
     */
    public Room addRoomObject(RoomObject roomObject, Point2Integer location)
    {
        PreCondition.assertNotNull(roomObject, "roomObject");
        PreCondition.assertNotNull(location, "location");
        PreCondition.assertFalse(this.isLocationOccupied(location), "this.isLocationOccupied(location)");

        this.locationToRoomObjectMap.set(location, roomObject);

        return this;
    }

    /**
     * Remove the {@link RoomObject} at the provided location.
     * @param location The location to remove a {@link RoomObject} from.
     * @return This object for method chaining.
     */
    public Room removeRoomObject(Point2Integer location)
    {
        PreCondition.assertNotNull(location, "location");
        PreCondition.assertTrue(this.isLocationOccupied(location), "this.isLocationOccupied(location)");

        this.locationToRoomObjectMap.remove(location).await();

        return this;
    }

    /**
     * Get the possible moves that are available on the board.
     */
    public Iterable<Move> getMoves()
    {
        final List<Move> result = List.create();

        for (final Point2Integer orbLocation : this.iterateRoomObjectLocations(RoomObject.Orb))
        {
            this.addUpMove(orbLocation, result::add);
            this.addRightMove(orbLocation, result::add);
            this.addDownMove(orbLocation, result::add);
            this.addLeftMove(orbLocation, result::add);
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public void addUpMove(Point2Integer orbLocation, Action1<Move> addMove)
    {
        PreCondition.assertNotNull(orbLocation, "orbLocation");
        PreCondition.assertNotNull(addMove, "addMove");

        MapEntry<Point2Integer,RoomObject> roomObjectResult = null;

        final int orbLocationX = orbLocation.getXAsInt();
        final int orbLocationY = orbLocation.getYAsInt();
        final RoomObject blockingRoomObject = this.getRoomObject(Point2Integer.create(orbLocationX, orbLocationY + 1))
            .catchError()
            .await();
        if (blockingRoomObject == null || blockingRoomObject == RoomObject.Goal)
        {
            for (final MapEntry<Point2Integer, RoomObject> roomObjectEntry : this.locationToRoomObjectMap)
            {
                final Point2Integer roomObjectLocation = roomObjectEntry.getKey();
                final int roomObjectX = roomObjectLocation.getXAsInt();
                if (orbLocationX == roomObjectX)
                {
                    final int roomObjectY = roomObjectLocation.getYAsInt();
                    if (roomObjectY < orbLocationY &&
                        (roomObjectResult == null || roomObjectResult.getKey().getYAsInt() < roomObjectY))
                    {
                        roomObjectResult = roomObjectEntry;
                    }
                }
            }

            Move move = null;
            if (roomObjectResult != null)
            {
                final Point2Integer roomObjectResultLocation = roomObjectResult.getKey();
                if (roomObjectResult.getValue() == RoomObject.Goal)
                {
                    move = Move.create()
                        .setStartLocation(orbLocation)
                        .setEndLocation(roomObjectResultLocation)
                        .setEndLocationIsGoal(true);
                }
                else
                {
                    final int roomObjectResultY = roomObjectResultLocation.getYAsInt();
                    if (roomObjectResultY < orbLocationY - 1)
                    {
                        final Point2Integer endLocation = Point2Integer.create(orbLocationX, roomObjectResultY + 1);
                        move = Move.create()
                            .setStartLocation(orbLocation)
                            .setEndLocation(endLocation);
                        if (roomObjectResult.getValue() == RoomObject.BreakableBlock)
                        {
                            move.setBlockBroken(true);
                        }
                    }
                }

                if (move != null)
                {
                    addMove.run(move);
                }
            }
        }
    }

    public void addDownMove(Point2Integer orbLocation, Action1<Move> addMove)
    {
        PreCondition.assertNotNull(orbLocation, "orbLocation");
        PreCondition.assertNotNull(addMove, "addMove");

        MapEntry<Point2Integer,RoomObject> roomObjectResult = null;

        final int orbLocationX = orbLocation.getXAsInt();
        final int orbLocationY = orbLocation.getYAsInt();
        final RoomObject blockingRoomObject = this.getRoomObject(Point2Integer.create(orbLocationX, orbLocationY - 1))
            .catchError()
            .await();
        if (blockingRoomObject == null || blockingRoomObject == RoomObject.Goal)
        {
            for (final MapEntry<Point2Integer, RoomObject> roomObjectEntry : this.locationToRoomObjectMap)
            {
                final Point2Integer roomObjectLocation = roomObjectEntry.getKey();
                final int roomObjectX = roomObjectLocation.getXAsInt();
                if (orbLocationX == roomObjectX)
                {
                    final int roomObjectY = roomObjectLocation.getYAsInt();
                    if (roomObjectY > orbLocationY &&
                        (roomObjectResult == null || roomObjectResult.getKey().getYAsInt() > roomObjectY))
                    {
                        roomObjectResult = roomObjectEntry;
                    }
                }
            }

            Move move = null;
            if (roomObjectResult != null)
            {
                final Point2Integer roomObjectResultLocation = roomObjectResult.getKey();
                if (roomObjectResult.getValue() == RoomObject.Goal)
                {
                    move = Move.create()
                        .setStartLocation(orbLocation)
                        .setEndLocation(roomObjectResultLocation)
                        .setEndLocationIsGoal(true);
                }
                else
                {
                    final int roomObjectResultY = roomObjectResultLocation.getYAsInt();
                    if (roomObjectResultY > orbLocationY + 1)
                    {
                        final Point2Integer endLocation = Point2Integer.create(orbLocationX, roomObjectResultY - 1);
                        move = Move.create()
                            .setStartLocation(orbLocation)
                            .setEndLocation(endLocation);
                        if (roomObjectResult.getValue() == RoomObject.BreakableBlock)
                        {
                            move.setBlockBroken(true);
                        }
                    }
                }

                if (move != null)
                {
                    addMove.run(move);
                }
            }
        }
    }

    public void addLeftMove(Point2Integer orbLocation, Action1<Move> addMove)
    {
        PreCondition.assertNotNull(orbLocation, "orbLocation");
        PreCondition.assertNotNull(addMove, "addMove");

        MapEntry<Point2Integer,RoomObject> roomObjectResult = null;

        final int orbLocationX = orbLocation.getXAsInt();
        final int orbLocationY = orbLocation.getYAsInt();
        final RoomObject blockingRoomObject = this.getRoomObject(Point2Integer.create(orbLocationX + 1, orbLocationY))
            .catchError()
            .await();
        if (blockingRoomObject == null || blockingRoomObject == RoomObject.Goal)
        {
            for (final MapEntry<Point2Integer, RoomObject> roomObjectEntry : this.locationToRoomObjectMap)
            {
                final Point2Integer roomObjectLocation = roomObjectEntry.getKey();
                final int roomObjectY = roomObjectLocation.getYAsInt();
                if (orbLocationY == roomObjectY)
                {
                    final int roomObjectX = roomObjectLocation.getXAsInt();
                    if (roomObjectX < orbLocationX &&
                        (roomObjectResult == null || roomObjectResult.getKey().getXAsInt() < roomObjectX))
                    {
                        roomObjectResult = roomObjectEntry;
                    }
                }
            }

            Move move = null;
            if (roomObjectResult != null)
            {
                final Point2Integer roomObjectResultLocation = roomObjectResult.getKey();
                if (roomObjectResult.getValue() == RoomObject.Goal)
                {
                    move = Move.create()
                        .setStartLocation(orbLocation)
                        .setEndLocation(roomObjectResultLocation)
                        .setEndLocationIsGoal(true);
                }
                else
                {
                    final int roomObjectResultX = roomObjectResultLocation.getXAsInt();
                    if (roomObjectResultX < orbLocationX - 1)
                    {
                        final Point2Integer endLocation = Point2Integer.create(roomObjectResultX + 1, orbLocationY);
                        move = Move.create()
                            .setStartLocation(orbLocation)
                            .setEndLocation(endLocation);
                        if (roomObjectResult.getValue() == RoomObject.BreakableBlock)
                        {
                            move.setBlockBroken(true);
                        }
                    }
                }

                if (move != null)
                {
                    addMove.run(move);
                }
            }
        }
    }

    public void addRightMove(Point2Integer orbLocation, Action1<Move> addMove)
    {
        PreCondition.assertNotNull(orbLocation, "orbLocation");
        PreCondition.assertNotNull(addMove, "addMove");

        MapEntry<Point2Integer,RoomObject> roomObjectResult = null;

        final int orbLocationX = orbLocation.getXAsInt();
        final int orbLocationY = orbLocation.getYAsInt();
        final RoomObject blockingRoomObject = this.getRoomObject(Point2Integer.create(orbLocationX - 1, orbLocationY))
            .catchError()
            .await();
        if (blockingRoomObject == null || blockingRoomObject == RoomObject.Goal)
        {
            for (final MapEntry<Point2Integer, RoomObject> roomObjectEntry : this.locationToRoomObjectMap)
            {
                final Point2Integer roomObjectLocation = roomObjectEntry.getKey();
                final int roomObjectY = roomObjectLocation.getYAsInt();
                if (orbLocationY == roomObjectY)
                {
                    final int roomObjectX = roomObjectLocation.getXAsInt();
                    if (roomObjectX > orbLocationX &&
                        (roomObjectResult == null || roomObjectResult.getKey().getXAsInt() > roomObjectX))
                    {
                        roomObjectResult = roomObjectEntry;
                    }
                }
            }

            Move move = null;
            if (roomObjectResult != null)
            {
                final Point2Integer roomObjectResultLocation = roomObjectResult.getKey();
                if (roomObjectResult.getValue() == RoomObject.Goal)
                {
                    move = Move.create()
                        .setStartLocation(orbLocation)
                        .setEndLocation(roomObjectResultLocation)
                        .setEndLocationIsGoal(true);
                }
                else
                {
                    final int roomObjectResultX = roomObjectResultLocation.getXAsInt();
                    if (roomObjectResultX > orbLocationX + 1)
                    {
                        final Point2Integer endLocation = Point2Integer.create(roomObjectResultX - 1, orbLocationY);
                        move = Move.create()
                            .setStartLocation(orbLocation)
                            .setEndLocation(endLocation);
                        if (roomObjectResult.getValue() == RoomObject.BreakableBlock)
                        {
                            move.setBlockBroken(true);
                        }
                    }
                }

                if (move != null)
                {
                    addMove.run(move);
                }
            }
        }
    }

    public void applyMove(Move move)
    {
        PreCondition.assertNotNull(move, "move");
        PreCondition.assertEqual(RoomObject.Orb, this.getRoomObject(move.getStartLocation()).catchError().await(), "this.getRoomObject(move.getStartLocation()).catchError().await()");
        PreCondition.assertTrue(!move.getBlockBroken() || this.getRoomObject(move.getBrokenBlockLocation()).await() == RoomObject.BreakableBlock, "!move.getBlockBroken() || this.getRoomObject(move.getBrokenBlockLocation()).await() == RoomObject.BreakableBlock");

        this.removeRoomObject(move.getStartLocation());

        if (!move.getEndLocationIsGoal())
        {
            this.addRoomObject(RoomObject.Orb, move.getEndLocation());

            if (move.getBlockBroken())
            {
                this.removeRoomObject(move.getBrokenBlockLocation());
            }
        }
    }

    public void undoMove(Move move)
    {
        PreCondition.assertNotNull(move, "move");
        PreCondition.assertFalse(this.isLocationOccupied(move.getStartLocation()), "this.isLocationOccupied(move.getStartLocation())");

        if (move.getBlockBroken())
        {
            this.addRoomObject(RoomObject.BreakableBlock, move.getBrokenBlockLocation());
        }

        if (!move.getEndLocationIsGoal())
        {
            this.removeRoomObject(move.getEndLocation());
        }

        this.addRoomObject(RoomObject.Orb, move.getStartLocation());
    }

    public Result<Iterable<Move>> findSolution()
    {
        return Result.create(() ->
        {
            Iterable<Move> result = null;

            final Queue<Tuple2<Room,Iterable<Move>>> roomsToVisit = Queue.create();
            roomsToVisit.enqueue(Tuple.create(this.clone(), Iterable.create()));

            final MutableSet<Room> visitedRoomStates = Set.create();

            while (roomsToVisit.any())
            {
                final Tuple2<Room,Iterable<Move>> roomAndMoveHistory = roomsToVisit.dequeue().await();

                final Room room = roomAndMoveHistory.getValue1();
                visitedRoomStates.add(room);

                final Iterable<Move> moveHistory = roomAndMoveHistory.getValue2();
                if (room.getRoomObjectCount(RoomObject.Orb) == 0)
                {
                    result = moveHistory;
                    break;
                }

                for (final Move move : room.getMoves())
                {
                    final Room newRoom = room.clone();
                    newRoom.applyMove(move);
                    if (!visitedRoomStates.contains(newRoom))
                    {
                        final Iterable<Move> newMoveHistory = List.create(moveHistory).add(move);
                        roomsToVisit.enqueue(Tuple.create(newRoom, newMoveHistory));
                    }
                }
            }

            if (result == null)
            {
                throw new NotFoundException("No solution exists for the provided room.");
            }

            PostCondition.assertNotNull(result, "result");

            return result;
        });
    }

    private Iterable<MapEntry<Point2Integer,RoomObject>> getSortedEntries()
    {
        final List<MapEntry<Point2Integer,RoomObject>> result = this.locationToRoomObjectMap.toList();
        result.sort((MapEntry<Point2Integer,RoomObject> lhs, MapEntry<Point2Integer,RoomObject> rhs) ->
        {
            final Point2Integer lhsLocation = lhs.getKey();
            final Point2Integer rhsLocation = rhs.getKey();
            return lhsLocation.getYAsInt() < rhsLocation.getYAsInt() ||
                (lhsLocation.getYAsInt() == rhsLocation.getYAsInt() && lhsLocation.getXAsInt() < rhsLocation.getXAsInt());
        });

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    @Override
    public String toString()
    {
        final Iterable<MapEntry<Point2Integer,RoomObject>> sortedEntries = this.getSortedEntries();

        final CharacterTable table = CharacterTable.create();
        final List<String> currentRow = List.create();
        for (final MapEntry<Point2Integer,RoomObject> entry : sortedEntries)
        {
            final Point2Integer entryLocation = entry.getKey();

            final int entryY = entryLocation.getYAsInt();
            while (table.getRows().getCount() < entryY)
            {
                table.addRow(currentRow);
                currentRow.clear();
            }

            final int entryX = entryLocation.getXAsInt();
            while (currentRow.getCount() < entryX)
            {
                currentRow.add(" ");
            }

            switch (entry.getValue())
            {
                case Orb:
                    currentRow.add("o");
                    break;

                case Goal:
                    currentRow.add("G");
                    break;

                case Block:
                    currentRow.add("B");
                    break;

                case BreakableBlock:
                    currentRow.add("X");
                    break;
            }
        }

        table.addRow(currentRow);

        final String result = table.toString(CharacterTableFormat.create()
            .setLeftBorder('|')
            .setRightBorder('|')
            .setTopBorder('-')
            .setBottomBorder('-')
            .setNewLine('\n'));

        PostCondition.assertNotNullAndNotEmpty(result, "result");

        return result;
    }

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof Room && this.equals((Room)rhs);
    }

    public boolean equals(Room rhs)
    {
        return rhs != null &&
            this.locationToRoomObjectMap.equals(rhs.locationToRoomObjectMap);
    }

    @Override
    public int hashCode()
    {
        return Hash.getHashCode(this.getSortedEntries());
    }

    @Override
    public Room clone()
    {
        final Room result = Room.create();
        for (final MapEntry<Point2Integer,RoomObject> entry : this.locationToRoomObjectMap)
        {
            result.addRoomObject(entry.getValue(), entry.getKey());
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }
}