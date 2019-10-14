/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package mozilla.components.feature.downloads

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.annotation.VisibleForTesting
import kotlinx.android.synthetic.main.mozac_downloads_prompt.*
import kotlinx.android.synthetic.main.mozac_downloads_prompt.view.*
import mozilla.components.feature.downloads.R.string.mozac_feature_downloads_dialog_download
import mozilla.components.feature.downloads.R.string.mozac_feature_downloads_dialog_title

private const val KEY_DIALOG_GRAVITY = "KEY_DIALOG_GRAVITY"
private const val KEY_DIALOG_WIDTH_MATCH_PARENT = "KEY_DIALOG_WIDTH_MATCH_PARENT"
private const val KEY_TITLE_ICON = "KEY_TITLE_ICON"
private const val KEY_POSITIVE_BUTTON_BACKGROUND_COLOR = "KEY_POSITIVE_BUTTON_BACKGROUND_COLOR"
private const val KEY_POSITIVE_BUTTON_TEXT_COLOR = "KEY_POSITIVE_BUTTON_TEXT_COLOR"
private const val KEY_SHOULD_SHOW_DO_NOT_ASK_AGAIN_CHECKBOX = "KEY_SHOULD_SHOW_DO_NOT_ASK_AGAIN_CHECKBOX"
private const val KEY_SHOULD_PRESELECT_DO_NOT_ASK_AGAIN_CHECKBOX = "KEY_SHOULD_PRESELECT_DO_NOT_ASK_AGAIN_CHECKBOX"
private const val KEY_IS_NOTIFICATION_REQUEST = "KEY_IS_NOTIFICATION_REQUEST"
private const val DEFAULT_VALUE = Int.MAX_VALUE

/**
 * A confirmation dialog to be called before a download is triggered.
 * Meant to be used in collaboration with [DownloadsFeature]
 *
 * [SimpleDownloadDialogFragment] is the default dialog use by DownloadsFeature if you don't provide a value.
 * It is composed by a title, a negative and a positive bottoms. When the positive button is clicked
 * the download it triggered.
 *
 */
class SimpleDownloadDialogFragment : DownloadDialogFragment() {

    private val safeArguments get() = requireNotNull(arguments)

    @VisibleForTesting
    internal var testingContext: Context? = null

    internal val dialogGravity: Int get() =
        safeArguments.getInt(KEY_DIALOG_GRAVITY, DEFAULT_VALUE)
    internal val dialogShouldWidthMatchParent: Boolean get() =
        safeArguments.getBoolean(KEY_DIALOG_WIDTH_MATCH_PARENT)

    /*
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        fun getBuilder(themeID: Int): AlertDialog.Builder {
            val context = testingContext ?: requireContext()
            return if (themeID == 0) AlertDialog.Builder(context) else AlertDialog.Builder(context, themeID)
        }

        return with(requireBundle()) {
            val fileName = getString(KEY_FILE_NAME, "")
            val dialogTitleText = getInt(KEY_TITLE_TEXT, mozac_feature_downloads_dialog_title)
            val positiveButtonText = getInt(KEY_DOWNLOAD_TEXT, mozac_feature_downloads_dialog_download)
            val negativeButtonText = getInt(KEY_NEGATIVE_TEXT, mozac_feature_downloads_dialog_cancel)
            val themeResId = getInt(KEY_THEME_ID, 0)
            val cancelable = getBoolean(KEY_CANCELABLE, false)

            getBuilder(themeResId)
                .setTitle(dialogTitleText)
                .setMessage(fileName)
                .setPositiveButton(positiveButtonText) { _, _ ->
                    onStartDownload()
                }
                .setNegativeButton(negativeButtonText) { _, _ ->
                    onCancelDownload()
                    dismiss()
                }
                .setOnCancelListener { onCancelDownload() }
                .setCancelable(cancelable)
                .create()
        }
    }
     */

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // TODO: Do I even need these separate container/dialog things?
        val sheetDialog = Dialog(requireContext())
        sheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        sheetDialog.setCanceledOnTouchOutside(true)

        val rootView = createContainer()

        sheetDialog.setContainerView(rootView)

        sheetDialog.window?.apply {

            if (dialogGravity != DEFAULT_VALUE) {
                setGravity(dialogGravity)
            }

            if (dialogShouldWidthMatchParent) {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                // This must be called after addContentView, or it won't fully fill to the edge.
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }

        return sheetDialog
    }

    @SuppressLint("InflateParams")
    private fun createContainer(): View {
        val rootView = LayoutInflater.from(requireContext()).inflate(
                R.layout.mozac_downloads_prompt,
                null,
                false
        )

        with(requireBundle()) {
            rootView.title.text = getString(KEY_TITLE_TEXT, "")
            rootView.filename.text = getString(KEY_FILE_NAME, "")
            rootView.download_button.text = getString(KEY_DOWNLOAD_TEXT, "")

            rootView.close_button.setOnClickListener {
                dismiss()
            }

            rootView.download_button.setOnClickListener {
                onStartDownload()
                dismiss()
            }


            // TODO: How does prompt styling work??
            feature?.promptsStyling?.apply {
                putInt(KEY_DIALOG_GRAVITY, gravity)
                putBoolean(KEY_DIALOG_WIDTH_MATCH_PARENT, shouldWidthMatchParent)

                positiveButtonBackgroundColor?.apply {
                    putInt(KEY_POSITIVE_BUTTON_BACKGROUND_COLOR, this)
                }

                positiveButtonTextColor?.apply {
                    putInt(KEY_POSITIVE_BUTTON_TEXT_COLOR, this)
                }
            }
        }

        return rootView
    }

    private fun Dialog.setContainerView(rootView: View) {
        if (dialogShouldWidthMatchParent) {
            setContentView(rootView)
        } else {
            addContentView(
                    rootView,
                    LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    )
            )
        }
    }


    companion object {
        /**
         * A builder method for creating a [SimpleDownloadDialogFragment]
         */
        fun newInstance(
            @StringRes dialogTitleText: Int = mozac_feature_downloads_dialog_title,
            @StringRes downloadButtonText: Int = mozac_feature_downloads_dialog_download,
            @StyleRes themeResId: Int = 0,
            cancelable: Boolean = false
        ): SimpleDownloadDialogFragment {
            val fragment = SimpleDownloadDialogFragment()
            val arguments = fragment.arguments ?: Bundle()

            with(arguments) {
                putInt(KEY_TITLE_TEXT, dialogTitleText)

                putInt(KEY_DOWNLOAD_TEXT, downloadButtonText)

                putInt(KEY_THEME_ID, themeResId)

                putBoolean(KEY_CANCELABLE, cancelable)

            }

            fragment.arguments = arguments

            return fragment
        }

        const val KEY_DOWNLOAD_TEXT = "KEY_DOWNLOAD_TEXT"

        const val KEY_TITLE_TEXT = "KEY_TITLE_TEXT"

        const val KEY_THEME_ID = "KEY_THEME_ID"

        const val KEY_CANCELABLE = "KEY_CANCELABLE"
    }

    private fun requireBundle(): Bundle {
        return arguments ?: throw IllegalStateException("Fragment $this arguments is not set.")
    }
}
