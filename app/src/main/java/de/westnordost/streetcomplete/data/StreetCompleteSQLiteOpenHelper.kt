package de.westnordost.streetcomplete.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesTable
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsTable
import de.westnordost.streetcomplete.data.osm.edits.EditElementsTable
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProviderTable
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsTable
import de.westnordost.streetcomplete.data.osm.geometry.RelationGeometryTable
import de.westnordost.streetcomplete.data.osm.geometry.WayGeometryTable
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenTable
import de.westnordost.streetcomplete.data.osmnotes.NoteTable
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsTable
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenTable
import de.westnordost.streetcomplete.data.user.achievements.UserAchievementsTable
import de.westnordost.streetcomplete.data.user.achievements.UserLinksTable
import de.westnordost.streetcomplete.data.user.statistics.ActiveDaysTable
import de.westnordost.streetcomplete.data.user.statistics.CountryStatisticsTables
import de.westnordost.streetcomplete.data.user.statistics.EditTypeStatisticsTables
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsTable
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderTable
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable
import de.westnordost.streetcomplete.quests.oneway_suspects.data.WayTrafficFlowTable

class StreetCompleteSQLiteOpenHelper(context: Context, dbName: String) :
    SQLiteOpenHelper(context, dbName, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // OSM notes
        db.execSQL(NoteTable.CREATE)
        db.execSQL(NoteTable.SPATIAL_INDEX_CREATE)

        // changes made on OSM notes
        db.execSQL(NoteEditsTable.CREATE)
        db.execSQL(NoteEditsTable.SPATIAL_INDEX_CREATE)
        db.execSQL(NoteEditsTable.NOTE_ID_INDEX_CREATE)

        // OSM map data
        db.execSQL(WayGeometryTable.CREATE)
        db.execSQL(RelationGeometryTable.CREATE)

        db.execSQL(NodeTable.CREATE)
        db.execSQL(NodeTable.SPATIAL_INDEX_CREATE)

        db.execSQL(WayTables.CREATE)
        db.execSQL(WayTables.NODES_CREATE)
        db.execSQL(WayTables.NODES_INDEX_CREATE)
        db.execSQL(WayTables.WAYS_BY_NODE_ID_INDEX_CREATE)

        db.execSQL(RelationTables.CREATE)
        db.execSQL(RelationTables.MEMBERS_CREATE)
        db.execSQL(RelationTables.MEMBERS_INDEX_CREATE)
        db.execSQL(RelationTables.MEMBERS_BY_ELEMENT_INDEX_CREATE)

        // changes made on OSM map data
        db.execSQL(ElementEditsTable.CREATE)
        db.execSQL(ElementIdProviderTable.CREATE)
        db.execSQL(ElementIdProviderTable.INDEX_CREATE)
        db.execSQL(ElementIdProviderTable.ELEMENT_INDEX_CREATE)

        db.execSQL(EditElementsTable.CREATE)
        db.execSQL(EditElementsTable.INDEX_CREATE)

        db.execSQL(CreatedElementsTable.CREATE)

        // quests
        db.execSQL(VisibleQuestTypeTable.CREATE)
        db.execSQL(QuestTypeOrderTable.CREATE)
        db.execSQL(QuestTypeOrderTable.INDEX_CREATE)
        db.execSQL(QuestPresetsTable.CREATE)

        // quests based on OSM elements
        db.execSQL(OsmQuestTable.CREATE)
        db.execSQL(OsmQuestTable.SPATIAL_INDEX_CREATE)
        db.execSQL(OsmQuestsHiddenTable.CREATE)

        // quests based on OSM notes
        db.execSQL(NoteQuestsHiddenTable.CREATE)

        // for upload / download
        db.execSQL(OpenChangesetsTable.CREATE)
        db.execSQL(DownloadedTilesTable.CREATE)

        // user statistics
        db.execSQL(EditTypeStatisticsTables.create(EditTypeStatisticsTables.NAME))
        db.execSQL(EditTypeStatisticsTables.create(EditTypeStatisticsTables.NAME_CURRENT_WEEK))
        db.execSQL(CountryStatisticsTables.create(CountryStatisticsTables.NAME))
        db.execSQL(CountryStatisticsTables.create(CountryStatisticsTables.NAME_CURRENT_WEEK))
        db.execSQL(UserAchievementsTable.CREATE)
        db.execSQL(UserLinksTable.CREATE)
        db.execSQL(ActiveDaysTable.CREATE)

        // quest specific tables
        db.execSQL(WayTrafficFlowTable.CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // for later changes to the DB
        // ...
        if (oldVersion <= 1 && newVersion > 1) {
            db.execSQL(CreatedElementsTable.CREATE)
        }
        if (oldVersion <= 2 && newVersion > 2) {
            db.execSQL(QuestTypeOrderTable.CREATE)
            db.execSQL(QuestTypeOrderTable.INDEX_CREATE)

            db.execSQL(QuestPresetsTable.CREATE)

            val oldName = "quest_visibility_old"
            db.execSQL("ALTER TABLE ${VisibleQuestTypeTable.NAME} RENAME TO $oldName;")
            db.execSQL(VisibleQuestTypeTable.CREATE)
            db.execSQL("""
                INSERT INTO ${VisibleQuestTypeTable.NAME} (
                    ${VisibleQuestTypeTable.Columns.QUEST_PRESET_ID},
                    ${VisibleQuestTypeTable.Columns.QUEST_TYPE},
                    ${VisibleQuestTypeTable.Columns.VISIBILITY}
                ) SELECT
                    0,
                    ${VisibleQuestTypeTable.Columns.QUEST_TYPE},
                    ${VisibleQuestTypeTable.Columns.VISIBILITY}
                FROM $oldName;
            """.trimIndent())
            db.execSQL("DROP TABLE $oldName;")
        }
        if (oldVersion <= 3 && newVersion > 3) {
            db.execSQL("DROP TABLE new_achievements")
        }
        if (oldVersion <= 4 && newVersion > 4) {
            db.execSQL(NodeTable.SPATIAL_INDEX_CREATE)
            db.execSQL(WayGeometryTable.CREATE)
            db.execSQL(RelationGeometryTable.CREATE)
            val oldGeometryTableName = "elements_geometry"
            val oldTypeName = "element_type"
            val oldIdName = "element_id"
            db.execSQL("""
                INSERT INTO ${WayGeometryTable.NAME} (
                    ${WayGeometryTable.Columns.ID},
                    ${WayGeometryTable.Columns.GEOMETRY_POLYLINES},
                    ${WayGeometryTable.Columns.GEOMETRY_POLYGONS},
                    ${WayGeometryTable.Columns.CENTER_LATITUDE},
                    ${WayGeometryTable.Columns.CENTER_LONGITUDE}
                ) SELECT
                    $oldIdName,
                    ${WayGeometryTable.Columns.GEOMETRY_POLYLINES},
                    ${WayGeometryTable.Columns.GEOMETRY_POLYGONS},
                    ${WayGeometryTable.Columns.CENTER_LATITUDE},
                    ${WayGeometryTable.Columns.CENTER_LONGITUDE}
                FROM
                    $oldGeometryTableName
                WHERE
                    $oldTypeName = 'WAY';
            """.trimIndent()
            )
            db.execSQL("""
                INSERT INTO ${RelationGeometryTable.NAME} (
                    ${RelationGeometryTable.Columns.ID},
                    ${RelationGeometryTable.Columns.GEOMETRY_POLYLINES},
                    ${RelationGeometryTable.Columns.GEOMETRY_POLYGONS},
                    ${RelationGeometryTable.Columns.CENTER_LATITUDE},
                    ${RelationGeometryTable.Columns.CENTER_LONGITUDE}
                ) SELECT
                    $oldIdName,
                    ${RelationGeometryTable.Columns.GEOMETRY_POLYLINES},
                    ${RelationGeometryTable.Columns.GEOMETRY_POLYGONS},
                    ${RelationGeometryTable.Columns.CENTER_LATITUDE},
                    ${RelationGeometryTable.Columns.CENTER_LONGITUDE}
                FROM
                    $oldGeometryTableName
                WHERE
                    $oldTypeName = 'RELATION';
            """.trimIndent()
            )
            db.execSQL("DROP TABLE $oldGeometryTableName;")
        }
        if (oldVersion <= 5 && newVersion > 5) {
            db.execSQL("ALTER TABLE ${NoteEditsTable.NAME} ADD COLUMN ${NoteEditsTable.Columns.TRACK} text DEFAULT '[]' NOT NULL")
        }
        if (oldVersion <= 6 && newVersion > 6) {
            db.execSQL(EditTypeStatisticsTables.create(EditTypeStatisticsTables.NAME_CURRENT_WEEK))
            db.execSQL(CountryStatisticsTables.create(CountryStatisticsTables.NAME_CURRENT_WEEK))
            db.execSQL(ActiveDaysTable.CREATE)
        }
        if (oldVersion <= 7 && newVersion > 7) {
            db.delete(ElementEditsTable.NAME, "${ElementEditsTable.Columns.QUEST_TYPE} = 'AddShoulder'", null)
        }
        if (oldVersion <= 8 && newVersion > 8) {
            db.renameQuest("AddPicnicTableCover", "AddAmenityCover")
        }
        if (oldVersion <= 9 && newVersion > 9) {
            db.execSQL("DROP TABLE ${DownloadedTilesTable.NAME};")
            db.execSQL(DownloadedTilesTable.CREATE)
        }
        if (oldVersion <= 10 && newVersion > 10) {
            db.execSQL("DROP INDEX osm_element_edits_index")

            // Recreating table (=clearing table) because it would be very complicated to pick the
            // data from the table in the old format and put it into the new format: the fields of
            // the serialized actions all changed
            db.execSQL("DROP TABLE ${ElementEditsTable.NAME};")
            db.execSQL(ElementEditsTable.CREATE)

            db.execSQL(EditElementsTable.CREATE)
            db.execSQL(EditElementsTable.INDEX_CREATE)

            db.execSQL(ElementIdProviderTable.ELEMENT_INDEX_CREATE)
        }
        if (oldVersion <= 11 && newVersion > 11) {
            // Deleting all data related to elements and their geometries, since
            // the binary format in which `GEOMETRY_{POLYGONS,POLYLINES}` data from
            // `{Way,Relation}GeometryTable` is stored has changed (see #5307)

            db.execSQL("DELETE FROM ${RelationTables.NAME_MEMBERS};")
            db.execSQL("DELETE FROM ${RelationTables.NAME};")

            db.execSQL("DELETE FROM ${WayTables.NAME_NODES};")
            db.execSQL("DELETE FROM ${WayTables.NAME};")

            db.execSQL("DELETE FROM ${NodeTable.NAME};")

            db.execSQL("DELETE FROM ${RelationGeometryTable.NAME};")
            db.execSQL("DELETE FROM ${WayGeometryTable.NAME};")

            db.execSQL("DELETE FROM ${DownloadedTilesTable.NAME};")
        }
    }
}

private const val DB_VERSION = 12

private fun SQLiteDatabase.renameQuest(old: String, new: String) {
    renameValue(ElementEditsTable.NAME, ElementEditsTable.Columns.QUEST_TYPE, old, new)
    renameValue(OsmQuestTable.NAME, OsmQuestTable.Columns.QUEST_TYPE, old, new)
    renameValue(OsmQuestsHiddenTable.NAME, OsmQuestsHiddenTable.Columns.QUEST_TYPE, old, new)
    renameValue(VisibleQuestTypeTable.NAME, VisibleQuestTypeTable.Columns.QUEST_TYPE, old, new)
    renameValue(OpenChangesetsTable.NAME, OpenChangesetsTable.Columns.QUEST_TYPE, old, new)
    renameValue(QuestTypeOrderTable.NAME, QuestTypeOrderTable.Columns.BEFORE, old, new)
    renameValue(QuestTypeOrderTable.NAME, QuestTypeOrderTable.Columns.AFTER, old, new)
}

private fun SQLiteDatabase.renameValue(table: String, column: String, oldValue: String, newValue: String) {
    update(table, contentValuesOf(column to newValue), "$column = ?", arrayOf(oldValue))
}
