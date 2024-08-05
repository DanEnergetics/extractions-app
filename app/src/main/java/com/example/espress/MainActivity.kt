package com.example.espress

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "extractions-db"
        ).fallbackToDestructiveMigration().build()
        val appViewModel = AppViewModel(
            db, contextProvider = { applicationContext }
        )

        val stopwatchViewModel: StopwatchViewModel by viewModels()

        setContent {
             MaterialTheme {
                 App(
                     extractionViewModel = appViewModel,
                     stopwatchViewModel = stopwatchViewModel,
                 )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    extractionViewModel: AppViewModel = viewModel(),
    stopwatchViewModel: StopwatchViewModel = viewModel(),
) {
    val extractions by extractionViewModel.allExtractions.collectAsState(initial = arrayListOf())
    val stopwatch = stopwatchViewModel.stopwatch

    AppLayout(
        extractions = extractions,
        onDeleteExtraction = extractionViewModel::deleteExtraction,
        onAddExtraction = extractionViewModel::insertExtraction,
        onUpdateExtraction = extractionViewModel::updateExtraction,
        stopwatch = stopwatch,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AppLayout(
    extractions: List<Extraction> = arrayListOf(Extraction()),
    onDeleteExtraction: (Extraction) -> Unit = {},
    onAddExtraction: (Extraction) -> Unit = {},
    onUpdateExtraction: (Extraction) -> Unit = {},
    stopwatch: StopwatchViewModel.Stopwatch? = null,
    ) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var isNewExtraction by remember { mutableStateOf(true) }
    var editExtraction by remember { mutableStateOf(Extraction()) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showBottomSheet = true
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "")
            }
        },
        topBar = {
            Row(Modifier.padding(10.dp)) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Extractions",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
        },
    ) { contentPadding ->
        // Screen content
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues = contentPadding)
                .padding(horizontal = 10.dp),
            contentPadding = PaddingValues(bottom=50.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(extractions) {extraction ->
                RowEntry(
                    extraction,
                    onDelete = { onDeleteExtraction(extraction) },
                    onEdit = {
                        editExtraction = extraction
                        isNewExtraction = false
                        showBottomSheet = true
                    }
                )
            }
        }

        if (showBottomSheet) {
            val initialData: Extraction
            val title: String
            val action: String
            if (isNewExtraction) {
                initialData = extractions.getOrElse(
                    extractions.lastIndex,
                    defaultValue = { Extraction(uid = 0, duration = 0L, grind = 0f, time = "") }
                )
                title = "Create Extraction"
                action = "Add"
            } else {
                initialData = editExtraction
                title = "Edit Extraction"
                action = "Save"
            }

            if (!isNewExtraction)
                stopwatch?.set(editExtraction.duration)

            val bottomPadding = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding().value.toInt().dp

            ModalBottomSheet(
//                modifier = Modifier.padding(contentPadding)
//                    .padding(top = 0.dp, start = 0.dp, end = 0.dp, bottom = bottomPadding),
                onDismissRequest = {
                    showBottomSheet = false
                    stopwatch?.reset()
                    isNewExtraction = true
                },
                sheetState = sheetState,
            ) {
                // Sheet content
                EditDataView(
                    onSave = { extraction ->
                        if (isNewExtraction) onAddExtraction(extraction)
                        else onUpdateExtraction(extraction)
                        isNewExtraction = true
                        stopwatch?.reset()
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    },
                    stopwatch = stopwatch,
                    initialData = initialData,
                    title = title,
                    action = action,
                    bottomPadding = contentPadding.calculateBottomPadding(),
//                    contentPadding = contentPadding.,
                )
            }
        }
    }
}

@Composable
fun MyRow(content: @Composable (RowScope.() -> Unit)) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
fun DataText(text: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.End) {
    Text(text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier,
        textAlign = textAlign,
    )
}

//@Preview
@Composable
fun RowEntry(
    extraction: Extraction = Extraction(
        uid = 0, duration = 5L, grind = 1f, time = "20.04.2024",
        grindTime = 96L, yield = 39f,
    ),
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    MyRow {
        arrayOf(
            extraction.time,
            extraction.duration.formatTime(),
            "%.2f".format(extraction.grind),
            extraction.grindTime.formatTime(),
            "%dg".format(extraction.yield.toInt()),
        ).forEach {
            DataText(it)
            Spacer(modifier = Modifier.width(20.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Drop-down menu to delete and TODO: edit
        Box {
            IconButton(
                onClick = {expanded = true},
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Localized description",
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Edit",
                        style = MaterialTheme.typography.bodyMedium,
                    ) },
                    onClick = { onEdit(); expanded = false },
                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Delete",
                        style = MaterialTheme.typography.bodyMedium,
                    ) },
                    onClick = { onDelete(); expanded = false },
                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) }
                )
            }
        }
    }
}

fun Int.pow(n: Int): Int {
    return List(n) { this }.reduce { acc, i -> acc * i }
}

fun roundToDecimal(value: Float, decimal: Int = 0): Float {
    if (decimal == 0) return value.toInt().toFloat()
    val decimalPower = 10.pow(decimal)
    return (value * decimalPower).roundToInt() / decimalPower.toFloat()
}

@Composable
fun SliderBox(
    title: String = "Title",
    sliderPosition: Float = 0f,
    upper: Float = 5f,
    lower: Float = 0f,
    steps: Int = 20,
    decimal: Int = 2,
    setValue: (Float) -> Unit = {},
) {
    MyRow {
        DataText(title, Modifier.width(60.dp))
        Spacer(Modifier.width(20.dp))
        Slider(
            value = sliderPosition,
            onValueChange = { setValue(roundToDecimal(it, decimal)) },
            steps = steps - 1,
            valueRange = lower..upper,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(20.dp))
        DataText(
            "%.${decimal}f".format(sliderPosition),
            modifier = Modifier.width(30.dp)
        )
    }
}

@Preview
@Composable
fun ScrollBox(
    title: String = "Title",
    value: Float = 0f,
    unit: String = "u",
    speed: Float = 0.005f,
    onValueChange: (Float) -> Unit = {},
) {
    Box(
        Modifier
            .size(width = 150.dp, height = 100.dp)
//            .height(100.dp)
            .scrollable(
                orientation = Orientation.Vertical,
                state = rememberScrollableState {
                    onValueChange(value - speed * it)
                    it
                }
            )
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.shapes.large
            )
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            title,
            Modifier.align(Alignment.TopStart),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            if (value > 0.0) roundToDecimal(value - 0.1f, 1).toString()
            else "",
            Modifier
                .align(Alignment.TopCenter)
                .width(80.dp),
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 16.sp, lineHeight = 30.sp),
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .5f),
            textAlign = TextAlign.End
        )
        Text(
            roundToDecimal(value, 1).toString(),
            Modifier.width(80.dp),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.End,
        )
        Text(
            roundToDecimal(value + 0.1f, 1).toString(),
            Modifier
                .align(Alignment.BottomCenter)
                .width(80.dp),
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 16.sp, lineHeight = 30.sp),
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .5f),
            textAlign = TextAlign.End
        )
        Text(
            unit,
            Modifier
                .align(Alignment.CenterEnd)
                .width(30.dp),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun StopwatchBox(stopwatch: StopwatchViewModel.Stopwatch) {
    val clockState by stopwatch.state.collectAsState()
    val clockTime by stopwatch.timer.collectAsState()
    MyRow {
        DataText("Duration", Modifier.width(60.dp))
        Spacer(Modifier.width(20.dp))
        IconButton(onClick = { stopwatch.play() }, Modifier.wrapContentWidth()) {
            Icon(
                when (clockState) {
                    StopwatchState.Idle -> painterResource(id = R.drawable.baseline_play_circle_24)
                    StopwatchState.Running -> painterResource(id = R.drawable.baseline_pause_circle_24)
                    StopwatchState.Paused -> painterResource(id = R.drawable.baseline_play_circle_24)
                },
                contentDescription = "Play or pause stopwatch"
            )
        }
        if (clockState == StopwatchState.Running || clockState == StopwatchState.Paused) {
            IconButton(onClick = { stopwatch.reset() }) {
                Icon(Icons.Rounded.Clear, contentDescription = "Reset stopwatch")
            }
        }
        Spacer(Modifier.weight(1f))
        DataText(clockTime.formatTime(), modifier = Modifier.align(Alignment.CenterVertically))
    }
}

@Preview
@Composable
fun StopwatchBoxV2(
    clockTime: Long = 0L,
    clockState: StopwatchState = StopwatchState.Running,
    onPlayPause: () -> Unit = {},
    onReset: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .size(height = 100.dp, width = 150.dp)
            .clickable { onPlayPause() }
            .background(
                color = when (clockState) {
                    StopwatchState.Idle -> MaterialTheme.colorScheme.primaryContainer
                    StopwatchState.Paused -> Color.Red
                    StopwatchState.Running -> Color.Green
                },
                shape = MaterialTheme.shapes.large,
            )
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        DataText("Duration",
            Modifier
                .width(60.dp)
                .align(Alignment.TopStart))
        IconButton(onClick = onPlayPause, Modifier.wrapContentSize()) {
            Icon(
                Icons.Outlined.PlayArrow,
                contentDescription = "Play or pause stopwatch",
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .1f),
                modifier = Modifier.size(70.dp)
            )
        }
        if (clockState == StopwatchState.Running || clockState == StopwatchState.Paused) {
            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = "Reset stopwatch")
            }
        }
        Text(
            clockTime.formatTime(),
            Modifier
                .align(Alignment.CenterEnd)
                .width(100.dp),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.End
        )
    }
}

@Preview
@Composable
fun EditDataView(
    stopwatch: StopwatchViewModel.Stopwatch? = null,
    onSave: (Extraction) -> Unit = {},
    initialData: Extraction = Extraction(),
    title: String = "Create Extraction",
    action: String = "Save",
    bottomPadding: Dp = 0.dp,
//    contentPadding: PaddingValues = PaddingValues(),
) {
    val duration by stopwatch?.timer?.collectAsState() ?: remember { mutableLongStateOf(0L) }
    val (grindTime, setGrindTime) = remember { mutableFloatStateOf(initialData.grindTime / 10f) }
    val (extraction, extractionSetter) = remember {
        mutableStateOf(initialData)
    }
    Column (modifier = Modifier
        .padding(10.dp)
        .padding(bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    // save extraction; in particular, extra parameters
                    onSave(extraction.copy(
                        duration = duration,
                        grindTime = (grindTime * 10).toLong()
                    ))
                }
            ) {
                Text(action)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ScrollBox(
                stringResource(id = R.string.grind_time),
                grindTime,
                unit = "s",
                onValueChange = setGrindTime
            )
            ScrollBox(
                stringResource(id = R.string.yield),
                extraction.yield,
                unit = "g",
            ) { extractionSetter(extraction.copy(yield = it)) }
        }
        if (stopwatch != null) {
            val clockTime by stopwatch.timer.collectAsState()
            val clockState by stopwatch.state.collectAsState()
            StopwatchBoxV2(
                clockTime, clockState,
                onPlayPause = stopwatch::play,
                onReset = stopwatch::reset,
            )
        }
        SliderBox(
            stringResource(R.string.grind_size),
            extraction.grind,
            upper = 5f,
            lower = 0f,
            steps = 20,
            decimal = 2,
        ) { extractionSetter(extraction.copy(grind = it)) }
    }
}
