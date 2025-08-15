package com.gpssimulator.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.gpssimulator.data.model.MovementType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RouteDao_Impl implements RouteDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RouteEntity> __insertionAdapterOfRouteEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<RouteEntity> __deletionAdapterOfRouteEntity;

  private final EntityDeletionOrUpdateAdapter<RouteEntity> __updateAdapterOfRouteEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCompletedRoutes;

  public RouteDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRouteEntity = new EntityInsertionAdapter<RouteEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `routes` (`id`,`name`,`startLatitude`,`startLongitude`,`totalDistance`,`estimatedDuration`,`movementType`,`createdAt`,`isCompleted`,`actualDuration`,`completedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RouteEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindDouble(3, entity.getStartLatitude());
        statement.bindDouble(4, entity.getStartLongitude());
        statement.bindDouble(5, entity.getTotalDistance());
        statement.bindLong(6, entity.getEstimatedDuration());
        final String _tmp = __converters.fromMovementType(entity.getMovementType());
        statement.bindString(7, _tmp);
        final Long _tmp_1 = __converters.fromTimestamp(entity.getCreatedAt());
        if (_tmp_1 == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, _tmp_1);
        }
        final int _tmp_2 = entity.isCompleted() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
        statement.bindLong(10, entity.getActualDuration());
        final Long _tmp_3 = __converters.fromTimestamp(entity.getCompletedAt());
        if (_tmp_3 == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, _tmp_3);
        }
      }
    };
    this.__deletionAdapterOfRouteEntity = new EntityDeletionOrUpdateAdapter<RouteEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `routes` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RouteEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfRouteEntity = new EntityDeletionOrUpdateAdapter<RouteEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `routes` SET `id` = ?,`name` = ?,`startLatitude` = ?,`startLongitude` = ?,`totalDistance` = ?,`estimatedDuration` = ?,`movementType` = ?,`createdAt` = ?,`isCompleted` = ?,`actualDuration` = ?,`completedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RouteEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindDouble(3, entity.getStartLatitude());
        statement.bindDouble(4, entity.getStartLongitude());
        statement.bindDouble(5, entity.getTotalDistance());
        statement.bindLong(6, entity.getEstimatedDuration());
        final String _tmp = __converters.fromMovementType(entity.getMovementType());
        statement.bindString(7, _tmp);
        final Long _tmp_1 = __converters.fromTimestamp(entity.getCreatedAt());
        if (_tmp_1 == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, _tmp_1);
        }
        final int _tmp_2 = entity.isCompleted() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
        statement.bindLong(10, entity.getActualDuration());
        final Long _tmp_3 = __converters.fromTimestamp(entity.getCompletedAt());
        if (_tmp_3 == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, _tmp_3);
        }
        statement.bindLong(12, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteCompletedRoutes = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM routes WHERE isCompleted = 1";
        return _query;
      }
    };
  }

  @Override
  public Object insertRoute(final RouteEntity route, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfRouteEntity.insertAndReturnId(route);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRoute(final RouteEntity route, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfRouteEntity.handle(route);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateRoute(final RouteEntity route, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfRouteEntity.handle(route);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCompletedRoutes(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCompletedRoutes.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteCompletedRoutes.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<RouteEntity>> getAllRoutes() {
    final String _sql = "SELECT * FROM routes ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"routes"}, new Callable<List<RouteEntity>>() {
      @Override
      @NonNull
      public List<RouteEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfStartLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "startLatitude");
          final int _cursorIndexOfStartLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "startLongitude");
          final int _cursorIndexOfTotalDistance = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDistance");
          final int _cursorIndexOfEstimatedDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedDuration");
          final int _cursorIndexOfMovementType = CursorUtil.getColumnIndexOrThrow(_cursor, "movementType");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfActualDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "actualDuration");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final List<RouteEntity> _result = new ArrayList<RouteEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RouteEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final double _tmpStartLatitude;
            _tmpStartLatitude = _cursor.getDouble(_cursorIndexOfStartLatitude);
            final double _tmpStartLongitude;
            _tmpStartLongitude = _cursor.getDouble(_cursorIndexOfStartLongitude);
            final double _tmpTotalDistance;
            _tmpTotalDistance = _cursor.getDouble(_cursorIndexOfTotalDistance);
            final long _tmpEstimatedDuration;
            _tmpEstimatedDuration = _cursor.getLong(_cursorIndexOfEstimatedDuration);
            final MovementType _tmpMovementType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfMovementType);
            _tmpMovementType = __converters.toMovementType(_tmp);
            final Date _tmpCreatedAt;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfCreatedAt);
            }
            final Date _tmp_2 = __converters.toTimestamp(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpCreatedAt = _tmp_2;
            }
            final boolean _tmpIsCompleted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_3 != 0;
            final long _tmpActualDuration;
            _tmpActualDuration = _cursor.getLong(_cursorIndexOfActualDuration);
            final Date _tmpCompletedAt;
            final Long _tmp_4;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            _tmpCompletedAt = __converters.toTimestamp(_tmp_4);
            _item = new RouteEntity(_tmpId,_tmpName,_tmpStartLatitude,_tmpStartLongitude,_tmpTotalDistance,_tmpEstimatedDuration,_tmpMovementType,_tmpCreatedAt,_tmpIsCompleted,_tmpActualDuration,_tmpCompletedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getRouteById(final long id, final Continuation<? super RouteEntity> $completion) {
    final String _sql = "SELECT * FROM routes WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RouteEntity>() {
      @Override
      @Nullable
      public RouteEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfStartLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "startLatitude");
          final int _cursorIndexOfStartLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "startLongitude");
          final int _cursorIndexOfTotalDistance = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDistance");
          final int _cursorIndexOfEstimatedDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedDuration");
          final int _cursorIndexOfMovementType = CursorUtil.getColumnIndexOrThrow(_cursor, "movementType");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfActualDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "actualDuration");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final RouteEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final double _tmpStartLatitude;
            _tmpStartLatitude = _cursor.getDouble(_cursorIndexOfStartLatitude);
            final double _tmpStartLongitude;
            _tmpStartLongitude = _cursor.getDouble(_cursorIndexOfStartLongitude);
            final double _tmpTotalDistance;
            _tmpTotalDistance = _cursor.getDouble(_cursorIndexOfTotalDistance);
            final long _tmpEstimatedDuration;
            _tmpEstimatedDuration = _cursor.getLong(_cursorIndexOfEstimatedDuration);
            final MovementType _tmpMovementType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfMovementType);
            _tmpMovementType = __converters.toMovementType(_tmp);
            final Date _tmpCreatedAt;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfCreatedAt);
            }
            final Date _tmp_2 = __converters.toTimestamp(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpCreatedAt = _tmp_2;
            }
            final boolean _tmpIsCompleted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_3 != 0;
            final long _tmpActualDuration;
            _tmpActualDuration = _cursor.getLong(_cursorIndexOfActualDuration);
            final Date _tmpCompletedAt;
            final Long _tmp_4;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            _tmpCompletedAt = __converters.toTimestamp(_tmp_4);
            _result = new RouteEntity(_tmpId,_tmpName,_tmpStartLatitude,_tmpStartLongitude,_tmpTotalDistance,_tmpEstimatedDuration,_tmpMovementType,_tmpCreatedAt,_tmpIsCompleted,_tmpActualDuration,_tmpCompletedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
