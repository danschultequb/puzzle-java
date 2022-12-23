package qub;

public interface RoomTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(Room.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final Room room = Room.create();
                test.assertNotNull(room, "room");
                test.assertEqual(0, room.getRoomObjectCount());
            });

            runner.testGroup("getRoomObject(Point2Integer)", () ->
            {
                final Action2<Point2Integer,Throwable> getRoomObjectErrorTest = (Point2Integer location, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(location), (Test test) ->
                    {
                        final Room room = Room.create();
                        test.assertThrows(() -> room.getRoomObject(location).await(),
                            expected);
                        test.assertEqual(0, room.getRoomObjectCount());
                    });
                };

                getRoomObjectErrorTest.run(null, new PreConditionFailure("location cannot be null."));
                getRoomObjectErrorTest.run(Point2Integer.create(0, 0), new NotFoundException("Could not find the provided key ({\"x\":\"0\",\"y\":\"0\"}) in this Map."));

                final Action2<Point2Integer,RoomObject> getRoomObjectTest = (Point2Integer location, RoomObject roomObject) ->
                {
                    runner.test("with " + roomObject + " at " + location, (Test test) ->
                    {
                        final Room room = Room.create();
                        room.addRoomObject(roomObject, location);

                        test.assertEqual(roomObject, room.getRoomObject(location).await());
                    });
                };

                getRoomObjectTest.run(Point2Integer.create(0, 0), RoomObject.Orb);
                getRoomObjectTest.run(Point2Integer.create(10, 5), RoomObject.Block);
                getRoomObjectTest.run(Point2Integer.create(1, 3), RoomObject.BreakableBlock);
                getRoomObjectTest.run(Point2Integer.create(4, 8), RoomObject.Goal);
            });

            runner.testGroup("isLocationOccupied(Point2Integer)", () ->
            {
                final Action2<Point2Integer,Throwable> isLocationOccupiedErrorTest = (Point2Integer location, Throwable expected) ->
                {
                    runner.test("with " + location, (Test test) ->
                    {
                        final Room room = Room.create();
                        test.assertThrows(() -> room.isLocationOccupied(location),
                            expected);
                        test.assertEqual(0, room.getRoomObjectCount());
                    });
                };

                isLocationOccupiedErrorTest.run(null, new PreConditionFailure("location cannot be null."));

                runner.test("with location that is empty", (Test test) ->
                {
                    final Room room = Room.create();
                    test.assertFalse(room.isLocationOccupied(Point2Integer.create(0, 0)));
                });

                runner.test("with location that is not empty", (Test test) ->
                {
                    final Room room = Room.create();
                    room.addRoomObject(RoomObject.Block, Point2Integer.create(0, 0));
                    test.assertTrue(room.isLocationOccupied(Point2Integer.create(0, 0)));
                });
            });

            runner.testGroup("iterateRoomObjectLocations(RoomObject)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final Room room = Room.create();
                    test.assertThrows(() -> room.iterateRoomObjectLocations(null),
                        new PreConditionFailure("roomObject cannot be null."));
                    test.assertEqual(0, room.getRoomObjectCount());
                });

                runner.test("with no room objects", (Test test) ->
                {
                    final Room room = Room.create();
                    test.assertEqual(Iterable.create(), room.iterateRoomObjectLocations(RoomObject.Orb).toList());
                });

                runner.test("with no matching room objects", (Test test) ->
                {
                    final Room room = Room.create()
                        .addRoomObject(RoomObject.Block, Point2Integer.create(1, 2))
                        .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(0, 0))
                        .addRoomObject(RoomObject.Goal, Point2Integer.create(10, 12));
                    test.assertEqual(Iterable.create(), room.iterateRoomObjectLocations(RoomObject.Orb).toList());
                });

                runner.test("with a matching room object", (Test test) ->
                {
                    final Room room = Room.create()
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(5, 7))
                        .addRoomObject(RoomObject.Block, Point2Integer.create(1, 2))
                        .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(0, 0))
                        .addRoomObject(RoomObject.Goal, Point2Integer.create(10, 12));
                    test.assertEqual(
                        Iterable.create(
                            Point2Integer.create(5, 7)),
                        room.iterateRoomObjectLocations(RoomObject.Orb).toList());
                });

                runner.test("with matching room objects", (Test test) ->
                {
                    final Room room = Room.create()
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(5, 7))
                        .addRoomObject(RoomObject.Block, Point2Integer.create(1, 2))
                        .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(0, 0))
                        .addRoomObject(RoomObject.Goal, Point2Integer.create(10, 12))
                        .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(3, 8))
                        .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(0, 1));
                    test.assertEqual(
                        Iterable.create(
                            Point2Integer.create(0, 0),
                            Point2Integer.create(3, 8),
                            Point2Integer.create(0, 1)),
                        room.iterateRoomObjectLocations(RoomObject.BreakableBlock).toList());
                });
            });

            runner.testGroup("addRoomObject(RoomObject,Point2Integer)", () ->
            {
                final Action3<RoomObject,Point2Integer,Throwable> addRoomObjectErrorTest = (RoomObject roomObject, Point2Integer location, Throwable expected) ->
                {
                    runner.test("with " + English.andList(roomObject, location), (Test test) ->
                    {
                        final Room room = Room.create();
                        test.assertThrows(() -> room.addRoomObject(roomObject, location),
                            expected);
                        test.assertEqual(0, room.getRoomObjectCount());
                    });
                };

                addRoomObjectErrorTest.run(null, Point2Integer.create(0, 0), new PreConditionFailure("roomObject cannot be null."));
                addRoomObjectErrorTest.run(RoomObject.Orb, null, new PreConditionFailure("location cannot be null."));

                runner.test("with an unoccupied space", (Test test) ->
                {
                    final Room room = Room.create();
                    final Room addRoomObjectResult = room.addRoomObject(RoomObject.Orb, Point2Integer.create(1, 2));
                    test.assertSame(room, addRoomObjectResult);
                    test.assertEqual(RoomObject.Orb, room.getRoomObject(Point2Integer.create(1, 2)).await());
                });

                runner.test("with occupied space", (Test test) ->
                {
                    final Room room = Room.create();
                    room.addRoomObject(RoomObject.Orb, Point2Integer.create(1, 2));
                    test.assertThrows(() -> room.addRoomObject(RoomObject.Block, Point2Integer.create(1, 2)),
                        new PreConditionFailure("this.isLocationOccupied(location) cannot be true."));
                    test.assertEqual(RoomObject.Orb, room.getRoomObject(Point2Integer.create(1, 2)).await());
                });
            });

            runner.testGroup("removeRoomObject(Point2Integer)", () ->
            {
                final Action2<Point2Integer,Throwable> removeRoomObjectErrorTest = (Point2Integer location, Throwable expected) ->
                {
                    runner.test("with " + location, (Test test) ->
                    {
                        final Room room = Room.create();
                        test.assertThrows(() -> room.removeRoomObject(location),
                            expected);
                        test.assertEqual(0, room.getRoomObjectCount());
                    });
                };

                removeRoomObjectErrorTest.run(null, new PreConditionFailure("location cannot be null."));

                runner.test("with an unoccupied space", (Test test) ->
                {
                    final Room room = Room.create();
                    test.assertThrows(() -> room.removeRoomObject(Point2Integer.create(1, 2)),
                        new PreConditionFailure("this.isLocationOccupied(location) cannot be false."));
                    test.assertEqual(0, room.getRoomObjectCount());
                });

                runner.test("with occupied space", (Test test) ->
                {
                    final Room room = Room.create();
                    room.addRoomObject(RoomObject.Orb, Point2Integer.create(1, 2));
                    final Room removeRoomObjectResult = room.removeRoomObject(Point2Integer.create(1, 2));
                    test.assertSame(room, removeRoomObjectResult);
                    test.assertFalse(room.isLocationOccupied(Point2Integer.create(1, 2)));
                });
            });

            runner.testGroup("addUpMove(Point2Integer,Action1<Move>)", () ->
            {
                runner.test("with null orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    test.assertThrows(() -> room.addUpMove(null, (Move move) -> {}),
                        new PreConditionFailure("orbLocation cannot be null."));
                });

                runner.test("with null addMove", (Test test) ->
                {
                    final Room room = Room.create();
                    test.assertThrows(() -> room.addUpMove(Point2Integer.create(), null),
                        new PreConditionFailure("addMove cannot be null."));
                });

                runner.test("with no room objects", (Test test) ->
                {
                    final Room room = Room.create();
                    final Point2Integer orbLocation = Point2Integer.create(0, 0);
                    final List<Move> moves = List.create();

                    room.addUpMove(orbLocation, moves::add);

                    test.assertEqual(Iterable.create(), moves);
                });

                runner.test("with no room objects above orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    room.addRoomObject(RoomObject.Orb, Point2Integer.create(1, 1));
                    room.addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(-1, 1));
                    final Point2Integer orbLocation = Point2Integer.create(0, 0);
                    final List<Move> moves = List.create();

                    room.addUpMove(orbLocation, moves::add);

                    test.assertEqual(Iterable.create(), moves);
                });

                final Action1<RoomObject> roomObjectOneSpaceAboveBallLocationTest = (RoomObject roomObject) ->
                {
                    runner.test("with " + roomObject + " one space above orbLocation", (Test test) ->
                    {
                        final Room room = Room.create();
                        room.addRoomObject(roomObject, Point2Integer.create(0, 0));
                        final Point2Integer orbLocation = Point2Integer.create(0, 1);
                        final List<Move> moves = List.create();

                        room.addUpMove(orbLocation, moves::add);

                        test.assertEqual(Iterable.create(), moves);
                    });
                };

                roomObjectOneSpaceAboveBallLocationTest.run(RoomObject.Orb);
                roomObjectOneSpaceAboveBallLocationTest.run(RoomObject.Block);
                roomObjectOneSpaceAboveBallLocationTest.run(RoomObject.BreakableBlock);

                runner.test("with " + RoomObject.Goal + " one space above orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    room.addRoomObject(RoomObject.Goal, Point2Integer.create(0, 0));
                    final Point2Integer orbLocation = Point2Integer.create(0, 1);
                    final List<Move> moves = List.create();

                    room.addUpMove(orbLocation, moves::add);

                    test.assertEqual(
                        Iterable.create(
                            Move.create()
                                .setStartLocation(orbLocation)
                                .setEndLocation(Point2Integer.create(0, 0))
                                .setEndLocationIsGoal(true)),
                        moves);
                });

                final Action1<RoomObject> roomObjectTwoSpacesAboveBallLocationTest = (RoomObject roomObject) ->
                {
                    runner.test("with " + roomObject + " two spaces above orbLocation", (Test test) ->
                    {
                        final Room room = Room.create();
                        room.addRoomObject(roomObject, Point2Integer.create(0, 0));
                        final Point2Integer orbLocation = Point2Integer.create(0, 2);
                        final List<Move> moves = List.create();

                        room.addUpMove(orbLocation, moves::add);

                        test.assertEqual(
                            Iterable.create(
                                Move.create()
                                    .setStartLocation(orbLocation)
                                    .setEndLocation(Point2Integer.create(0, 1))
                                    .setBlockBroken(roomObject == RoomObject.BreakableBlock)),
                            moves);
                    });
                };

                roomObjectTwoSpacesAboveBallLocationTest.run(RoomObject.Orb);
                roomObjectTwoSpacesAboveBallLocationTest.run(RoomObject.Block);
                roomObjectTwoSpacesAboveBallLocationTest.run(RoomObject.BreakableBlock);

                runner.test("with " + RoomObject.Goal + " two spaces above orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    room.addRoomObject(RoomObject.Goal, Point2Integer.create(0, 0));
                    final Point2Integer orbLocation = Point2Integer.create(0, 2);
                    final List<Move> moves = List.create();

                    room.addUpMove(orbLocation, moves::add);

                    test.assertEqual(
                        Iterable.create(
                            Move.create()
                                .setStartLocation(orbLocation)
                                .setEndLocation(Point2Integer.create(0, 0))
                                .setEndLocationIsGoal(true)),
                        moves);
                });

                final Action1<RoomObject> roomObjectOneSpaceBelowBallLocationTest = (RoomObject roomObject) ->
                {
                    runner.test("with " + roomObject + " one space below orbLocation", (Test test) ->
                    {
                        final Room room = Room.create();
                        room.addRoomObject(roomObject, Point2Integer.create(0, 0));
                        final Point2Integer orbLocation = Point2Integer.create(0, 2);
                        final Point2Integer blockingRoomObjectLocation = Point2Integer.create(0, 3);
                        room.addRoomObject(roomObject, blockingRoomObjectLocation);
                        final List<Move> moves = List.create();

                        room.addUpMove(orbLocation, moves::add);

                        test.assertEqual(
                            Iterable.create(),
                            moves);
                    });
                };

                roomObjectOneSpaceBelowBallLocationTest.run(RoomObject.Orb);
                roomObjectOneSpaceBelowBallLocationTest.run(RoomObject.Block);
                roomObjectOneSpaceBelowBallLocationTest.run(RoomObject.BreakableBlock);

                runner.test("with " + RoomObject.Goal + " two spaces above orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    room.addRoomObject(RoomObject.Goal, Point2Integer.create(0, 0));
                    final Point2Integer orbLocation = Point2Integer.create(0, 2);
                        final Point2Integer blockingRoomObjectLocation = Point2Integer.create(0, 3);
                        room.addRoomObject(RoomObject.Goal, blockingRoomObjectLocation);
                    final List<Move> moves = List.create();

                    room.addUpMove(orbLocation, moves::add);

                    test.assertEqual(
                        Iterable.create(
                            Move.create()
                                .setStartLocation(orbLocation)
                                .setEndLocation(Point2Integer.create(0, 0))
                                .setEndLocationIsGoal(true)),
                        moves);
                });
            });

            runner.testGroup("addDownMove(Point2Integer,Action1<Move>)", () ->
            {
                runner.test("with null orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    test.assertThrows(() -> room.addDownMove(null, (Move move) -> {}),
                        new PreConditionFailure("orbLocation cannot be null."));
                });

                runner.test("with null addMove", (Test test) ->
                {
                    final Room room = Room.create();
                    test.assertThrows(() -> room.addDownMove(Point2Integer.create(), null),
                        new PreConditionFailure("addMove cannot be null."));
                });

                runner.test("with no room objects", (Test test) ->
                {
                    final Room room = Room.create();
                    final Point2Integer orbLocation = Point2Integer.create(0, 0);
                    final List<Move> moves = List.create();

                    room.addDownMove(orbLocation, moves::add);

                    test.assertEqual(Iterable.create(), moves);
                });

                runner.test("with no room objects below orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    room.addRoomObject(RoomObject.Orb, Point2Integer.create(1, 1));
                    room.addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(-1, 1));
                    final Point2Integer orbLocation = Point2Integer.create(0, 0);
                    final List<Move> moves = List.create();

                    room.addDownMove(orbLocation, moves::add);

                    test.assertEqual(Iterable.create(), moves);
                });

                final Action1<RoomObject> roomObjectOneSpaceBelowBallLocationTest = (RoomObject roomObject) ->
                {
                    runner.test("with " + roomObject + " one space below orbLocation", (Test test) ->
                    {
                        final Room room = Room.create();
                        final Point2Integer roomObjectLocation = Point2Integer.create(0, 1);
                        room.addRoomObject(roomObject, roomObjectLocation);
                        final Point2Integer orbLocation = Point2Integer.create(0, 0);
                        final List<Move> moves = List.create();

                        room.addDownMove(orbLocation, moves::add);

                        test.assertEqual(Iterable.create(), moves);
                    });
                };

                roomObjectOneSpaceBelowBallLocationTest.run(RoomObject.Orb);
                roomObjectOneSpaceBelowBallLocationTest.run(RoomObject.Block);
                roomObjectOneSpaceBelowBallLocationTest.run(RoomObject.BreakableBlock);

                runner.test("with " + RoomObject.Goal + " one space below orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    final Point2Integer roomObjectLocation = Point2Integer.create(0, 1);
                    room.addRoomObject(RoomObject.Goal, roomObjectLocation);
                    final Point2Integer orbLocation = Point2Integer.create(0, 0);
                    final List<Move> moves = List.create();

                    room.addDownMove(orbLocation, moves::add);

                    test.assertEqual(
                        Iterable.create(
                            Move.create()
                                .setStartLocation(orbLocation)
                                .setEndLocation(roomObjectLocation)
                                .setEndLocationIsGoal(true)),
                        moves);
                });

                final Action1<RoomObject> roomObjectTwoSpacesBelowBallLocationTest = (RoomObject roomObject) ->
                {
                    runner.test("with " + roomObject + " two spaces below orbLocation", (Test test) ->
                    {
                        final Room room = Room.create();
                        final Point2Integer roomObjectLocation = Point2Integer.create(0, 2);
                        room.addRoomObject(roomObject, roomObjectLocation);
                        final Point2Integer orbLocation = Point2Integer.create(0, 0);
                        final List<Move> moves = List.create();

                        room.addDownMove(orbLocation, moves::add);

                        test.assertEqual(
                            Iterable.create(
                                Move.create()
                                    .setStartLocation(orbLocation)
                                    .setEndLocation(Point2Integer.create(0, 1))
                                    .setBlockBroken(roomObject == RoomObject.BreakableBlock)),
                            moves);
                    });
                };

                roomObjectTwoSpacesBelowBallLocationTest.run(RoomObject.Orb);
                roomObjectTwoSpacesBelowBallLocationTest.run(RoomObject.Block);
                roomObjectTwoSpacesBelowBallLocationTest.run(RoomObject.BreakableBlock);

                runner.test("with " + RoomObject.Goal + " two spaces below orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    final Point2Integer roomObjectLocation = Point2Integer.create(0, 2);
                    room.addRoomObject(RoomObject.Goal, roomObjectLocation);
                    final Point2Integer orbLocation = Point2Integer.create(0, 0);
                    final List<Move> moves = List.create();

                    room.addDownMove(orbLocation, moves::add);

                    test.assertEqual(
                        Iterable.create(
                            Move.create()
                                .setStartLocation(orbLocation)
                                .setEndLocation(roomObjectLocation)
                                .setEndLocationIsGoal(true)),
                        moves);
                });

                final Action1<RoomObject> roomObjectOneSpaceAboveBallLocationTest = (RoomObject roomObject) ->
                {
                    runner.test("with " + roomObject + " one space above orbLocation", (Test test) ->
                    {
                        final Room room = Room.create();
                        final Point2Integer roomObjectLocation = Point2Integer.create(0, 2);
                        room.addRoomObject(roomObject, roomObjectLocation);
                        final Point2Integer orbLocation = Point2Integer.create(0, 0);
                        final Point2Integer blockingRoomObjectLocation = Point2Integer.create(0, -1);
                        room.addRoomObject(roomObject, blockingRoomObjectLocation);
                        final List<Move> moves = List.create();

                        room.addDownMove(orbLocation, moves::add);

                        test.assertEqual(
                            Iterable.create(),
                            moves);
                    });
                };

                roomObjectOneSpaceAboveBallLocationTest.run(RoomObject.Orb);
                roomObjectOneSpaceAboveBallLocationTest.run(RoomObject.Block);
                roomObjectOneSpaceAboveBallLocationTest.run(RoomObject.BreakableBlock);

                runner.test("with " + RoomObject.Goal + " one space above orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    final Point2Integer roomObjectLocation = Point2Integer.create(0, 2);
                    room.addRoomObject(RoomObject.Goal, roomObjectLocation);
                    final Point2Integer orbLocation = Point2Integer.create(0, 0);
                    final Point2Integer blockingRoomObjectLocation = Point2Integer.create(0, -1);
                    room.addRoomObject(RoomObject.Goal, blockingRoomObjectLocation);
                    final List<Move> moves = List.create();

                    room.addDownMove(orbLocation, moves::add);

                    test.assertEqual(
                        Iterable.create(
                            Move.create()
                                .setStartLocation(orbLocation)
                                .setEndLocation(roomObjectLocation)
                                .setEndLocationIsGoal(true)),
                        moves);
                });
            });

            runner.testGroup("addLeftMove(Point2Integer,Action1<Move>)", () ->
            {
                runner.test("with null orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    test.assertThrows(() -> room.addLeftMove(null, (Move move) -> {}),
                        new PreConditionFailure("orbLocation cannot be null."));
                });

                runner.test("with null addMove", (Test test) ->
                {
                    final Room room = Room.create();
                    test.assertThrows(() -> room.addLeftMove(Point2Integer.create(), null),
                        new PreConditionFailure("addMove cannot be null."));
                });

                runner.test("with no room objects", (Test test) ->
                {
                    final Room room = Room.create();
                    final Point2Integer orbLocation = Point2Integer.create(0, 0);
                    final List<Move> moves = List.create();

                    room.addLeftMove(orbLocation, moves::add);

                    test.assertEqual(Iterable.create(), moves);
                });

                runner.test("with no room objects left of orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    room.addRoomObject(RoomObject.Orb, Point2Integer.create(1, 1));
                    room.addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(-1, 1));
                    final Point2Integer orbLocation = Point2Integer.create(0, 0);
                    final List<Move> moves = List.create();

                    room.addLeftMove(orbLocation, moves::add);

                    test.assertEqual(Iterable.create(), moves);
                });

                final Action1<RoomObject> roomObjectOneSpaceLeftOfBallLocationTest = (RoomObject roomObject) ->
                {
                    runner.test("with " + roomObject + " one space above orbLocation", (Test test) ->
                    {
                        final Room room = Room.create();
                        room.addRoomObject(roomObject, Point2Integer.create(0, 0));
                        final Point2Integer orbLocation = Point2Integer.create(1, 0);
                        final List<Move> moves = List.create();

                        room.addLeftMove(orbLocation, moves::add);

                        test.assertEqual(Iterable.create(), moves);
                    });
                };

                roomObjectOneSpaceLeftOfBallLocationTest.run(RoomObject.Orb);
                roomObjectOneSpaceLeftOfBallLocationTest.run(RoomObject.Block);
                roomObjectOneSpaceLeftOfBallLocationTest.run(RoomObject.BreakableBlock);

                runner.test("with " + RoomObject.Goal + " one space left of orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    final Point2Integer roomObjectLocation = Point2Integer.create(0, 0);
                    room.addRoomObject(RoomObject.Goal, roomObjectLocation);
                    final Point2Integer orbLocation = Point2Integer.create(1, 0);
                    final List<Move> moves = List.create();

                    room.addLeftMove(orbLocation, moves::add);

                    test.assertEqual(
                        Iterable.create(
                            Move.create()
                                .setStartLocation(orbLocation)
                                .setEndLocation(roomObjectLocation)
                                .setEndLocationIsGoal(true)),
                        moves);
                });

                final Action1<RoomObject> roomObjectTwoSpacesLeftOfBallLocationTest = (RoomObject roomObject) ->
                {
                    runner.test("with " + roomObject + " two spaces left of orbLocation", (Test test) ->
                    {
                        final Room room = Room.create();
                        final Point2Integer roomObjectLocation = Point2Integer.create(0, 0);
                        room.addRoomObject(roomObject, roomObjectLocation);
                        final Point2Integer orbLocation = Point2Integer.create(2, 0);
                        final List<Move> moves = List.create();

                        room.addLeftMove(orbLocation, moves::add);

                        test.assertEqual(
                            Iterable.create(
                                Move.create()
                                    .setStartLocation(orbLocation)
                                    .setEndLocation(Point2Integer.create(1, 0))
                                    .setBlockBroken(roomObject == RoomObject.BreakableBlock)),
                            moves);
                    });
                };

                roomObjectTwoSpacesLeftOfBallLocationTest.run(RoomObject.Orb);
                roomObjectTwoSpacesLeftOfBallLocationTest.run(RoomObject.Block);
                roomObjectTwoSpacesLeftOfBallLocationTest.run(RoomObject.BreakableBlock);

                runner.test("with " + RoomObject.Goal + " two spaces left of orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    final Point2Integer roomObjectLocation = Point2Integer.create(0, 0);
                    room.addRoomObject(RoomObject.Goal, roomObjectLocation);
                    final Point2Integer orbLocation = Point2Integer.create(2, 0);
                    final List<Move> moves = List.create();

                    room.addLeftMove(orbLocation, moves::add);

                    test.assertEqual(
                        Iterable.create(
                            Move.create()
                                .setStartLocation(orbLocation)
                                .setEndLocation(roomObjectLocation)
                                .setEndLocationIsGoal(true)),
                        moves);
                });

                final Action1<RoomObject> roomObjectOneSpaceRightOfBallLocationTest = (RoomObject roomObject) ->
                {
                    runner.test("with " + roomObject + " one space right of orbLocation", (Test test) ->
                    {
                        final Room room = Room.create();
                        final Point2Integer roomObjectLocation = Point2Integer.create(0, 0);
                        room.addRoomObject(roomObject, roomObjectLocation);
                        final Point2Integer orbLocation = Point2Integer.create(2, 0);
                        final Point2Integer blockingRoomObjectLocation = Point2Integer.create(3, 0);
                        room.addRoomObject(roomObject, blockingRoomObjectLocation);
                        final List<Move> moves = List.create();

                        room.addLeftMove(orbLocation, moves::add);

                        test.assertEqual(
                            Iterable.create(),
                            moves);
                    });
                };

                roomObjectOneSpaceRightOfBallLocationTest.run(RoomObject.Orb);
                roomObjectOneSpaceRightOfBallLocationTest.run(RoomObject.Block);
                roomObjectOneSpaceRightOfBallLocationTest.run(RoomObject.BreakableBlock);

                runner.test("with " + RoomObject.Goal + " one space right of orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    final Point2Integer roomObjectLocation = Point2Integer.create(0, 0);
                    room.addRoomObject(RoomObject.Goal, roomObjectLocation);
                    final Point2Integer orbLocation = Point2Integer.create(2, 0);
                    final Point2Integer blockingRoomObjectLocation = Point2Integer.create(3, 0);
                    room.addRoomObject(RoomObject.Goal, blockingRoomObjectLocation);
                    final List<Move> moves = List.create();

                    room.addLeftMove(orbLocation, moves::add);

                    test.assertEqual(
                        Iterable.create(
                            Move.create()
                                .setStartLocation(orbLocation)
                                .setEndLocation(roomObjectLocation)
                                .setEndLocationIsGoal(true)),
                        moves);
                });
            });

            runner.testGroup("addRightMove(Point2Integer,Action1<Move>)", () ->
            {
                runner.test("with null orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    test.assertThrows(() -> room.addRightMove(null, (Move move) -> {}),
                        new PreConditionFailure("orbLocation cannot be null."));
                });

                runner.test("with null addMove", (Test test) ->
                {
                    final Room room = Room.create();
                    test.assertThrows(() -> room.addRightMove(Point2Integer.create(), null),
                        new PreConditionFailure("addMove cannot be null."));
                });

                runner.test("with no room objects", (Test test) ->
                {
                    final Room room = Room.create();
                    final Point2Integer orbLocation = Point2Integer.create(0, 0);
                    final List<Move> moves = List.create();

                    room.addRightMove(orbLocation, moves::add);

                    test.assertEqual(Iterable.create(), moves);
                });

                runner.test("with no room objects right of orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    room.addRoomObject(RoomObject.Orb, Point2Integer.create(1, 1));
                    room.addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(-1, 1));
                    final Point2Integer orbLocation = Point2Integer.create(0, 0);
                    final List<Move> moves = List.create();

                    room.addRightMove(orbLocation, moves::add);

                    test.assertEqual(Iterable.create(), moves);
                });

                final Action1<RoomObject> roomObjectOneSpaceRightOfBallLocationTest = (RoomObject roomObject) ->
                {
                    runner.test("with " + roomObject + " one space above orbLocation", (Test test) ->
                    {
                        final Room room = Room.create();
                        final Point2Integer roomObjectLocation = Point2Integer.create(1, 0);
                        room.addRoomObject(roomObject, roomObjectLocation);
                        final Point2Integer orbLocation = Point2Integer.create(0, 0);
                        final List<Move> moves = List.create();

                        room.addRightMove(orbLocation, moves::add);

                        test.assertEqual(Iterable.create(), moves);
                    });
                };

                roomObjectOneSpaceRightOfBallLocationTest.run(RoomObject.Orb);
                roomObjectOneSpaceRightOfBallLocationTest.run(RoomObject.Block);
                roomObjectOneSpaceRightOfBallLocationTest.run(RoomObject.BreakableBlock);

                runner.test("with " + RoomObject.Goal + " one space right of orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    final Point2Integer roomObjectLocation = Point2Integer.create(1, 0);
                    room.addRoomObject(RoomObject.Goal, roomObjectLocation);
                    final Point2Integer orbLocation = Point2Integer.create(0, 0);
                    final List<Move> moves = List.create();

                    room.addRightMove(orbLocation, moves::add);

                    test.assertEqual(
                        Iterable.create(
                            Move.create()
                                .setStartLocation(orbLocation)
                                .setEndLocation(roomObjectLocation)
                                .setEndLocationIsGoal(true)),
                        moves);
                });

                final Action1<RoomObject> roomObjectTwoSpacesRightOfBallLocationTest = (RoomObject roomObject) ->
                {
                    runner.test("with " + roomObject + " two spaces right of orbLocation", (Test test) ->
                    {
                        final Room room = Room.create();
                        final Point2Integer roomObjectLocation = Point2Integer.create(2, 0);
                        room.addRoomObject(roomObject, roomObjectLocation);
                        final Point2Integer orbLocation = Point2Integer.create(0, 0);
                        final List<Move> moves = List.create();

                        room.addRightMove(orbLocation, moves::add);

                        test.assertEqual(
                            Iterable.create(
                                Move.create()
                                    .setStartLocation(orbLocation)
                                    .setEndLocation(Point2Integer.create(1, 0))
                                    .setBlockBroken(roomObject == RoomObject.BreakableBlock)),
                            moves);
                    });
                };

                roomObjectTwoSpacesRightOfBallLocationTest.run(RoomObject.Orb);
                roomObjectTwoSpacesRightOfBallLocationTest.run(RoomObject.Block);
                roomObjectTwoSpacesRightOfBallLocationTest.run(RoomObject.BreakableBlock);

                runner.test("with " + RoomObject.Goal + " two spaces right of orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    final Point2Integer roomObjectLocation = Point2Integer.create(2, 0);
                    room.addRoomObject(RoomObject.Goal, roomObjectLocation);
                    final Point2Integer orbLocation = Point2Integer.create(0, 0);
                    final List<Move> moves = List.create();

                    room.addRightMove(orbLocation, moves::add);

                    test.assertEqual(
                        Iterable.create(
                            Move.create()
                                .setStartLocation(orbLocation)
                                .setEndLocation(roomObjectLocation)
                                .setEndLocationIsGoal(true)),
                        moves);
                });

                final Action1<RoomObject> roomObjectOneSpaceLeftOfBallLocationTest = (RoomObject roomObject) ->
                {
                    runner.test("with " + roomObject + " two spaces right of orbLocation", (Test test) ->
                    {
                        final Room room = Room.create();
                        final Point2Integer roomObjectLocation = Point2Integer.create(2, 0);
                        room.addRoomObject(roomObject, roomObjectLocation);
                        final Point2Integer orbLocation = Point2Integer.create(0, 0);
                        final Point2Integer blockingRoomObjectLocation = Point2Integer.create(-1, 0);
                        room.addRoomObject(roomObject, blockingRoomObjectLocation);
                        final List<Move> moves = List.create();

                        room.addRightMove(orbLocation, moves::add);

                        test.assertEqual(
                            Iterable.create(),
                            moves);
                    });
                };

                roomObjectOneSpaceLeftOfBallLocationTest.run(RoomObject.Orb);
                roomObjectOneSpaceLeftOfBallLocationTest.run(RoomObject.Block);
                roomObjectOneSpaceLeftOfBallLocationTest.run(RoomObject.BreakableBlock);

                runner.test("with " + RoomObject.Goal + " one space left of orbLocation", (Test test) ->
                {
                    final Room room = Room.create();
                    final Point2Integer roomObjectLocation = Point2Integer.create(2, 0);
                    room.addRoomObject(RoomObject.Goal, roomObjectLocation);
                    final Point2Integer orbLocation = Point2Integer.create(0, 0);
                    final Point2Integer blockingRoomObjectLocation = Point2Integer.create(-1, 0);
                    room.addRoomObject(RoomObject.Goal, blockingRoomObjectLocation);
                    final List<Move> moves = List.create();

                    room.addRightMove(orbLocation, moves::add);

                    test.assertEqual(
                        Iterable.create(
                            Move.create()
                                .setStartLocation(orbLocation)
                                .setEndLocation(roomObjectLocation)
                                .setEndLocationIsGoal(true)),
                        moves);
                });
            });

            runner.testGroup("getMoves()", () ->
            {
                runner.test("with no room objects", (Test test) ->
                {
                    final Room room = Room.create();
                    test.assertEqual(Iterable.create(), room.getMoves());
                });

                runner.test("with no orbs", (Test test) ->
                {
                    final Room room = Room.create();
                    room.addRoomObject(RoomObject.Block, Point2Integer.create(1, 3));
                    room.addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(6, 3));

                    test.assertEqual(Iterable.create(), room.getMoves());
                });

                runner.test("with one orb but no other room objects", (Test test) ->
                {
                    final Room room = Room.create();
                    room.addRoomObject(RoomObject.Orb, Point2Integer.create(5, 7));

                    test.assertEqual(Iterable.create(), room.getMoves());
                });

                runner.test("with lots of room objects and 3 orbs", (Test test) ->
                {
                    final Room room = Room.create()
                        .addRoomObject(RoomObject.Block, Point2Integer.create(5, 0))
                        .addRoomObject(RoomObject.Block, Point2Integer.create(9, 0))
                        .addRoomObject(RoomObject.Block, Point2Integer.create(1, 1))
                        .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(3, 1))
                        .addRoomObject(RoomObject.Block, Point2Integer.create(11, 1))
                        .addRoomObject(RoomObject.Goal, Point2Integer.create(14, 1))
                        .addRoomObject(RoomObject.Goal, Point2Integer.create(15, 1))
                        .addRoomObject(RoomObject.Goal, Point2Integer.create(16, 1))
                        .addRoomObject(RoomObject.Goal, Point2Integer.create(14, 2))
                        .addRoomObject(RoomObject.Goal, Point2Integer.create(15, 2))
                        .addRoomObject(RoomObject.Goal, Point2Integer.create(16, 2))
                        .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(10, 3))
                        .addRoomObject(RoomObject.Goal, Point2Integer.create(14, 3))
                        .addRoomObject(RoomObject.Goal, Point2Integer.create(15, 3))
                        .addRoomObject(RoomObject.Goal, Point2Integer.create(16, 3))
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(1, 5))
                        .addRoomObject(RoomObject.Block, Point2Integer.create(7, 5))
                        .addRoomObject(RoomObject.Block, Point2Integer.create(12, 5))
                        .addRoomObject(RoomObject.Block, Point2Integer.create(0, 6))
                        .addRoomObject(RoomObject.Block, Point2Integer.create(10, 7))
                        .addRoomObject(RoomObject.Block, Point2Integer.create(10, 8))
                        .addRoomObject(RoomObject.Block, Point2Integer.create(2, 9))
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(4, 9))
                        .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(11, 9))
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(8, 10));
                    test.assertEqual(
                        Iterable.create(
                            Move.create()
                                .setStartLocation(Point2Integer.create(1, 5))
                                .setEndLocation(Point2Integer.create(1, 2)),
                            Move.create()
                                .setStartLocation(Point2Integer.create(1, 5))
                                .setEndLocation(Point2Integer.create(6, 5)),
                            Move.create()
                                .setStartLocation(Point2Integer.create(4, 9))
                                .setEndLocation(Point2Integer.create(10, 9))
                                .setBlockBroken(true),
                            Move.create()
                                .setStartLocation(Point2Integer.create(4, 9))
                                .setEndLocation(Point2Integer.create(3, 9))),
                        room.getMoves());
                });
            });

            runner.testGroup("applyMove(Move)", () ->
            {
                runner.test("with null move", (Test test) ->
                {
                    final Room room = Room.create();
                    test.assertThrows(() -> room.applyMove(null),
                        new PreConditionFailure("move cannot be null."));
                });

                runner.test("with no room object at start location", (Test test) ->
                {
                    final Room room = Room.create();
                    final Move move = Move.create()
                        .setStartLocation(Point2Integer.create(0, 0))
                        .setEndLocation(Point2Integer.create(3, 0));
                    test.assertThrows(() -> room.applyMove(move),
                        new PreConditionFailure("this.getRoomObject(move.getStartLocation()).catchError().await() (null) must be Orb."));
                });

                runner.test("with valid move into a block", (Test test) ->
                {
                    final Room room = Room.create()
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(0, 0))
                        .addRoomObject(RoomObject.Block, Point2Integer.create(4, 0));
                    final Move move = Move.create()
                        .setStartLocation(Point2Integer.create(0, 0))
                        .setEndLocation(Point2Integer.create(3, 0));

                    room.applyMove(move);

                    test.assertEqual(
                        Room.create()
                            .addRoomObject(RoomObject.Orb, Point2Integer.create(3, 0))
                            .addRoomObject(RoomObject.Block, Point2Integer.create(4, 0)),
                        room);
                });

                runner.test("with valid move into another orb", (Test test) ->
                {
                    final Room room = Room.create()
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(0, 0))
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(4, 0));
                    final Move move = Move.create()
                        .setStartLocation(Point2Integer.create(0, 0))
                        .setEndLocation(Point2Integer.create(3, 0));

                    room.applyMove(move);

                    test.assertEqual(
                        Room.create()
                            .addRoomObject(RoomObject.Orb, Point2Integer.create(3, 0))
                            .addRoomObject(RoomObject.Orb, Point2Integer.create(4, 0)),
                        room);
                });

                runner.test("with valid move into a breakable block", (Test test) ->
                {
                    final Room room = Room.create()
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(0, 0))
                        .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(4, 0));
                    final Move move = Move.create()
                        .setStartLocation(Point2Integer.create(0, 0))
                        .setEndLocation(Point2Integer.create(3, 0))
                        .setBlockBroken(true);

                    room.applyMove(move);

                    test.assertEqual(
                        Room.create()
                            .addRoomObject(RoomObject.Orb, Point2Integer.create(3, 0)),
                        room);
                });

                runner.test("with valid move into a goal", (Test test) ->
                {
                    final Room room = Room.create()
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(0, 0))
                        .addRoomObject(RoomObject.Goal, Point2Integer.create(3, 0));
                    final Move move = Move.create()
                        .setStartLocation(Point2Integer.create(0, 0))
                        .setEndLocation(Point2Integer.create(3, 0))
                        .setEndLocationIsGoal(true);

                    room.applyMove(move);

                    test.assertEqual(
                        Room.create()
                            .addRoomObject(RoomObject.Goal, Point2Integer.create(3, 0)),
                        room);
                });
            });

            runner.testGroup("undoMove(Move)", () ->
            {
                runner.test("with null move", (Test test) ->
                {
                    final Room room = Room.create();
                    test.assertThrows(() -> room.undoMove(null),
                        new PreConditionFailure("move cannot be null."));
                });

                runner.test("with non-" + RoomObject.Orb + " at start location", (Test test) ->
                {
                    final Room room = Room.create()
                        .addRoomObject(RoomObject.Block, Point2Integer.create(0, 0));
                    final Move move = Move.create()
                        .setStartLocation(Point2Integer.create(0, 0))
                        .setEndLocation(Point2Integer.create(3, 0));
                    test.assertThrows(() -> room.undoMove(move),
                        new PreConditionFailure("this.isLocationOccupied(move.getStartLocation()) cannot be true."));
                });

                runner.test("with valid move into a block", (Test test) ->
                {
                    final Room room = Room.create()
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(3, 0))
                        .addRoomObject(RoomObject.Block, Point2Integer.create(4, 0));
                    final Move move = Move.create()
                        .setStartLocation(Point2Integer.create(0, 0))
                        .setEndLocation(Point2Integer.create(3, 0));

                    room.undoMove(move);

                    test.assertEqual(
                        Room.create()
                            .addRoomObject(RoomObject.Orb, Point2Integer.create(0, 0))
                            .addRoomObject(RoomObject.Block, Point2Integer.create(4, 0)),
                        room);
                });

                runner.test("with valid move into another orb", (Test test) ->
                {
                    final Room room = Room.create()
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(3, 0))
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(4, 0));
                    final Move move = Move.create()
                        .setStartLocation(Point2Integer.create(0, 0))
                        .setEndLocation(Point2Integer.create(3, 0));

                    room.undoMove(move);

                    test.assertEqual(
                        Room.create()
                            .addRoomObject(RoomObject.Orb, Point2Integer.create(0, 0))
                            .addRoomObject(RoomObject.Orb, Point2Integer.create(4, 0)),
                        room);
                });

                runner.test("with valid move into a breakable block", (Test test) ->
                {
                    final Room room = Room.create()
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(3, 0));
                    final Move move = Move.create()
                        .setStartLocation(Point2Integer.create(0, 0))
                        .setEndLocation(Point2Integer.create(3, 0))
                        .setBlockBroken(true);

                    room.undoMove(move);

                    test.assertEqual(
                        Room.create()
                            .addRoomObject(RoomObject.Orb, Point2Integer.create(0, 0))
                            .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(4, 0)),
                        room);
                });

                runner.test("with valid move into a goal", (Test test) ->
                {
                    final Room room = Room.create()
                        .addRoomObject(RoomObject.Goal, Point2Integer.create(3, 0));
                    final Move move = Move.create()
                        .setStartLocation(Point2Integer.create(0, 0))
                        .setEndLocation(Point2Integer.create(3, 0))
                        .setEndLocationIsGoal(true);

                    room.undoMove(move);

                    test.assertEqual(
                        Room.create()
                            .addRoomObject(RoomObject.Orb, Point2Integer.create(0, 0))
                            .addRoomObject(RoomObject.Goal, Point2Integer.create(3, 0)),
                        room);
                });
            });

            runner.testGroup("findSolution()", () ->
            {
                final Action2<String,Room> findSolutionErrorTest = (String testName, Room room) ->
                {
                    runner.test(testName, (Test test) ->
                    {
                        test.assertThrows(() -> room.findSolution().await(),
                            new NotFoundException("No solution exists for the provided room."));
                    });
                };

                findSolutionErrorTest.run("with no " + RoomObject.Goal + "s",
                    Room.create()
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(0, 0)));
                findSolutionErrorTest.run("with no solution",
                    Room.create()
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(0, 0))
                        .addRoomObject(RoomObject.Goal, Point2Integer.create(1, 1)));

                final Action3<String,Room,Iterable<Move>> findSolutionTest = (String testName, Room room, Iterable<Move> expected) ->
                {
                    runner.test(testName,
                        (TestResources resources) -> Tuple.create(resources.getCurrentFolder()),
                        (Test test, Folder currentFolder) ->
                    {
                        final Room originalRoom = room.clone();

                        final Iterable<Move> solution = room.findSolution().await();
                        test.assertEqual(expected, solution);

                        test.assertEqual(originalRoom, room);

//                        final File file = currentFolder.getFile(testName + ".txt").await();
//                        try (final IndentedCharacterWriteStream writeStream = IndentedCharacterWriteStream.create(file.getContentsCharacterWriteStream().await()))
//                        {
//                            int moveNumber = 0;
//                            final Iterator<Move> solutionIterator = solution.iterate();
//                            while (!solutionIterator.hasStarted() || solutionIterator.hasCurrent())
//                            {
//                                writeStream.write(moveNumber + ": ").await();
//                                if (!solutionIterator.hasStarted())
//                                {
//                                    writeStream.writeLine("Original room").await();
//                                }
//                                else
//                                {
//                                    writeStream.writeLine(solutionIterator.getCurrent().toString()).await();
//                                }
//
//                                writeStream.indent(() ->
//                                {
//                                    writeStream.writeLine(room.toString()).await();
//                                    writeStream.writeLine().await();
//                                });
//
//                                if (solutionIterator.next())
//                                {
//                                    moveNumber++;
//                                    room.applyMove(solutionIterator.getCurrent());
//                                }
//                            }
//                        }
                    });
                };

                findSolutionTest.run("with no room objects",
                    Room.create(),
                    Iterable.create());
                findSolutionTest.run("with no " + RoomObject.Orb + "s",
                    Room.create()
                        .addRoomObject(RoomObject.Block, Point2Integer.create(0, 0)),
                    Iterable.create());
                findSolutionTest.run("with one move solution",
                    Room.create()
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(0, 0))
                        .addRoomObject(RoomObject.Goal, Point2Integer.create(1, 0)),
                    Iterable.create(
                        Move.create()
                            .setStartLocation(Point2Integer.create(0, 0))
                            .setEndLocation(Point2Integer.create(1, 0))
                            .setEndLocationIsGoal(true)));
                findSolutionTest.run("with two move solution",
                    Room.create()
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(0, 0))
                        .addRoomObject(RoomObject.Block, Point2Integer.create(3, 0))
                        .addRoomObject(RoomObject.Goal, Point2Integer.create(2, 2)),
                    Iterable.create(
                        Move.create()
                            .setStartLocation(Point2Integer.create(0, 0))
                            .setEndLocation(Point2Integer.create(2, 0)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(2, 0))
                            .setEndLocation(Point2Integer.create(2, 2))
                            .setEndLocationIsGoal(true)));
                findSolutionTest.run("1st Puzzle with Orbs",
                    RoomTests.get1stPuzzleWithOrbsRoom(),
                    Iterable.create(
                        Move.create()
                            .setStartLocation(Point2Integer.create(6, 11))
                            .setEndLocation(Point2Integer.create(9, 11))
                            .setBlockBroken(true),
                        Move.create()
                            .setStartLocation(Point2Integer.create(10, 14))
                            .setEndLocation(Point2Integer.create(10, 1)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(9, 11))
                            .setEndLocation(Point2Integer.create(9, 5)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(10, 1))
                            .setEndLocation(Point2Integer.create(13, 1)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(9, 5))
                            .setEndLocation(Point2Integer.create(12, 5))
                            .setEndLocationIsGoal(true),
                        Move.create()
                            .setStartLocation(Point2Integer.create(13, 1))
                            .setEndLocation(Point2Integer.create(13, 4))
                            .setEndLocationIsGoal(true)));
                findSolutionTest.run("2nd Puzzle with Orbs",
                    RoomTests.get2ndPuzzleWithOrbsRoom(),
                    Iterable.create(
                        Move.create()
                            .setStartLocation(Point2Integer.create(8, 11))
                            .setEndLocation(Point2Integer.create(4, 11)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(4, 11))
                            .setEndLocation(Point2Integer.create(4, 1)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(12, 11))
                            .setEndLocation(Point2Integer.create(4, 11)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(4, 11))
                            .setEndLocation(Point2Integer.create(4, 2)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(4, 2))
                            .setEndLocation(Point2Integer.create(11, 2)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(11, 2))
                            .setEndLocation(Point2Integer.create(11, 1))
                            .setBlockBroken(true),
                        Move.create()
                            .setStartLocation(Point2Integer.create(4, 1))
                            .setEndLocation(Point2Integer.create(10, 1)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(11, 1))
                            .setEndLocation(Point2Integer.create(11, 5))
                            .setEndLocationIsGoal(true),
                        Move.create()
                            .setStartLocation(Point2Integer.create(10, 1))
                            .setEndLocation(Point2Integer.create(10, 5))
                            .setEndLocationIsGoal(true)));
                findSolutionTest.run("3rd Puzzle with Orbs",
                    RoomTests.get3rdPuzzleWithOrbsRoom(),
                    Iterable.create(
                        Move.create()
                            .setStartLocation(Point2Integer.create(7, 4))
                            .setEndLocation(Point2Integer.create(15, 4)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(2, 5))
                            .setEndLocation(Point2Integer.create(2, 13))
                            .setBlockBroken(true),
                        Move.create()
                            .setStartLocation(Point2Integer.create(15, 4))
                            .setEndLocation(Point2Integer.create(15, 13)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(2, 13))
                            .setEndLocation(Point2Integer.create(2, 3)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(15, 13))
                            .setEndLocation(Point2Integer.create(2, 13)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(2, 3))
                            .setEndLocation(Point2Integer.create(10, 3))
                            .setEndLocationIsGoal(true),
                        Move.create()
                            .setStartLocation(Point2Integer.create(2, 13))
                            .setEndLocation(Point2Integer.create(2, 3)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(2, 3))
                            .setEndLocation(Point2Integer.create(10, 3))
                            .setEndLocationIsGoal(true)));
                findSolutionTest.run("4th Puzzle with Orbs",
                    RoomTests.get4thPuzzleWithOrbsRoom(),
                    Iterable.create(
                        Move.create()
                            .setStartLocation(Point2Integer.create(3, 9))
                            .setEndLocation(Point2Integer.create(3, 6)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(7, 13))
                            .setEndLocation(Point2Integer.create(7, 9))
                            .setBlockBroken(true),
                        Move.create()
                            .setStartLocation(Point2Integer.create(7, 9))
                            .setEndLocation(Point2Integer.create(7, 5)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(7, 5))
                            .setEndLocation(Point2Integer.create(12, 5)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(12, 5))
                            .setEndLocation(Point2Integer.create(12, 6))
                            .setBlockBroken(true),
                        Move.create()
                            .setStartLocation(Point2Integer.create(3, 6))
                            .setEndLocation(Point2Integer.create(11, 6)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(12, 6))
                            .setEndLocation(Point2Integer.create(12, 10)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(12, 10))
                            .setEndLocation(Point2Integer.create(3, 10)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(3, 10))
                            .setEndLocation(Point2Integer.create(3, 6)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(3, 6))
                            .setEndLocation(Point2Integer.create(10, 6)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(10, 14))
                            .setEndLocation(Point2Integer.create(10, 7)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(11, 6))
                            .setEndLocation(Point2Integer.create(11, 5)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(10, 6))
                            .setEndLocation(Point2Integer.create(16, 6))
                            .setEndLocationIsGoal(true),
                        Move.create()
                            .setStartLocation(Point2Integer.create(10, 7))
                            .setEndLocation(Point2Integer.create(16, 7))
                            .setEndLocationIsGoal(true),
                        Move.create()
                            .setStartLocation(Point2Integer.create(11, 5))
                            .setEndLocation(Point2Integer.create(12, 5)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(12, 5))
                            .setEndLocation(Point2Integer.create(12, 10)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(12, 10))
                            .setEndLocation(Point2Integer.create(3, 10)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(3, 10))
                            .setEndLocation(Point2Integer.create(3, 6)),
                        Move.create()
                            .setStartLocation(Point2Integer.create(3, 6))
                            .setEndLocation(Point2Integer.create(16, 6))
                            .setEndLocationIsGoal(true)));
            });

            runner.testGroup("toString()", () ->
            {
                final Action3<String,Room,Iterable<String>> toStringTest = (String testName, Room room, Iterable<String> expected) ->
                {
                    runner.test(testName, (Test test) ->
                    {
                        test.assertLinesEqual(expected, room.toString());
                    });
                };

                toStringTest.run("with empty room",
                    Room.create(),
                    Iterable.create(
                        "--",
                        "||",
                        "--"));
                toStringTest.run("with one orb in the top left corner",
                    Room.create()
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(0, 0)),
                    Iterable.create(
                        "---",
                        "|o|",
                        "---"));
                toStringTest.run("with one orb in the top right corner",
                    Room.create()
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(5, 0)),
                    Iterable.create(
                        "--------",
                        "|     o|",
                        "--------"));
                toStringTest.run("with one orb in the bottom left corner",
                    Room.create()
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(0, 4)),
                    Iterable.create(
                        "---",
                        "| |",
                        "| |",
                        "| |",
                        "| |",
                        "|o|",
                        "---"));
                toStringTest.run("with one orb in the bottom right corner",
                    Room.create()
                        .addRoomObject(RoomObject.Orb, Point2Integer.create(3, 4)),
                    Iterable.create(
                        "------",
                        "|    |",
                        "|    |",
                        "|    |",
                        "|    |",
                        "|   o|",
                        "------"));
                toStringTest.run("with breakable blocks and blocks on different rows",
                    Room.create()
                        .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(1, 1))
                        .addRoomObject(RoomObject.Block, Point2Integer.create(3, 2))
                        .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(0, 2))
                        .addRoomObject(RoomObject.Block, Point2Integer.create(4, 0)),
                    Iterable.create(
                        "-------",
                        "|    B|",
                        "| X   |",
                        "|X  B |",
                        "-------"));
                toStringTest.run("3rd Puzzle with Orbs",
                    RoomTests.get3rdPuzzleWithOrbsRoom(),
                    Iterable.create(
                        "-------------------",
                        "|                 |",
                        "|          GGG    |",
                        "|  B       GGG    |",
                        "|          GGG    |",
                        "|       o        B|",
                        "|  o         B    |",
                        "|              X  |",
                        "|       X         |",
                        "|                 |",
                        "|                 |",
                        "|                 |",
                        "|                 |",
                        "|       B         |",
                        "| B               |",
                        "|  X            B |",
                        "|             B   |",
                        "-------------------"
                    ));
                toStringTest.run("4th Puzzle with Orbs",
                    RoomTests.get4thPuzzleWithOrbsRoom(),
                    Iterable.create(
                        "---------------------",
                        "|                   |",
                        "|                   |",
                        "|                   |",
                        "|                   |",
                        "|       B   B       |",
                        "|   B X       B  GGG|",
                        "|                GGG|",
                        "|            X   GGG|",
                        "|       X           |",
                        "|   o     B    B    |",
                        "|  B                |",
                        "|            B      |",
                        "|            B      |",
                        "|    B  o     X     |",
                        "|          o        |",
                        "---------------------"));
            });
        });
    }

    public static Room get1stPuzzleWithOrbsRoom()
    {
        return Room.create()
            .addRoomObject(RoomObject.Block, Point2Integer.create(10, 0))
            .addRoomObject(RoomObject.Block, Point2Integer.create(14, 1))
            .addRoomObject(RoomObject.Block, Point2Integer.create(1, 3))
            .addRoomObject(RoomObject.Block, Point2Integer.create(9, 4))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(12, 4))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(13, 4))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(14, 4))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(12, 5))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(13, 5))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(14, 5))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(12, 6))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(13, 6))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(14, 6))
            .addRoomObject(RoomObject.Block, Point2Integer.create(0, 11))
            .addRoomObject(RoomObject.Orb, Point2Integer.create(6, 11))
            .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(10, 11))
            .addRoomObject(RoomObject.Block, Point2Integer.create(16, 12))
            .addRoomObject(RoomObject.Orb, Point2Integer.create(10, 14));
    }

    public static Room get2ndPuzzleWithOrbsRoom()
    {
        return Room.create()
            .addRoomObject(RoomObject.Block, Point2Integer.create(4, 0))
            .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(11, 0))
            .addRoomObject(RoomObject.Block, Point2Integer.create(14, 0))
            .addRoomObject(RoomObject.Block, Point2Integer.create(12, 2))
            .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(2, 3))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(9, 5))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(10, 5))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(11, 5))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(9, 6))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(10, 6))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(11, 6))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(9, 7))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(10, 7))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(11, 7))
            .addRoomObject(RoomObject.Block, Point2Integer.create(3, 11))
            .addRoomObject(RoomObject.Orb, Point2Integer.create(8, 11))
            .addRoomObject(RoomObject.Orb, Point2Integer.create(12, 11))
            .addRoomObject(RoomObject.Block, Point2Integer.create(15, 11));
    }

    public static Room get3rdPuzzleWithOrbsRoom()
    {
        return Room.create()
            .addRoomObject(RoomObject.Goal, Point2Integer.create(10, 1))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(11, 1))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(12, 1))
            .addRoomObject(RoomObject.Block, Point2Integer.create(2, 2))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(10, 2))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(11, 2))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(12, 2))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(10, 3))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(11, 3))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(12, 3))
            .addRoomObject(RoomObject.Orb, Point2Integer.create(7, 4))
            .addRoomObject(RoomObject.Block, Point2Integer.create(16, 4))
            .addRoomObject(RoomObject.Orb, Point2Integer.create(2, 5))
            .addRoomObject(RoomObject.Block, Point2Integer.create(12, 5))
            .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(14, 6))
            .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(7, 7))
            .addRoomObject(RoomObject.Block, Point2Integer.create(7, 12))
            .addRoomObject(RoomObject.Block, Point2Integer.create(1, 13))
            .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(2, 14))
            .addRoomObject(RoomObject.Block, Point2Integer.create(15, 14))
            .addRoomObject(RoomObject.Block, Point2Integer.create(13, 15));
    }

    public static Room get4thPuzzleWithOrbsRoom()
    {
        return Room.create()
            .addRoomObject(RoomObject.Block, Point2Integer.create(7, 4))
            .addRoomObject(RoomObject.Block, Point2Integer.create(11, 4))
            .addRoomObject(RoomObject.Block, Point2Integer.create(3, 5))
            .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(5, 5))
            .addRoomObject(RoomObject.Block, Point2Integer.create(13, 5))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(16, 5))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(17, 5))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(18, 5))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(16, 6))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(17, 6))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(18, 6))
            .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(12, 7))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(16, 7))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(17, 7))
            .addRoomObject(RoomObject.Goal, Point2Integer.create(18, 7))
            .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(7, 8))
            .addRoomObject(RoomObject.Orb, Point2Integer.create(3, 9))
            .addRoomObject(RoomObject.Block, Point2Integer.create(9, 9))
            .addRoomObject(RoomObject.Block, Point2Integer.create(14, 9))
            .addRoomObject(RoomObject.Block, Point2Integer.create(2, 10))
            .addRoomObject(RoomObject.Block, Point2Integer.create(12, 11))
            .addRoomObject(RoomObject.Block, Point2Integer.create(12, 12))
            .addRoomObject(RoomObject.Block, Point2Integer.create(4, 13))
            .addRoomObject(RoomObject.Orb, Point2Integer.create(7, 13))
            .addRoomObject(RoomObject.BreakableBlock, Point2Integer.create(13, 13))
            .addRoomObject(RoomObject.Orb, Point2Integer.create(10, 14));
    }
}
