/**
 *  Copyright (c) 2013-2015 Angelo ZERR and Genuitec LLC.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Piotr Tomiak <piotr@genuitec.com> - support for tern.js debugging
 */
package ts.server.nodejs.process;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ts.TSException;
import ts.server.nodejs.internal.process.NodeJSProcess;

/**
 * {@link NodejsProcess} manager.
 * 
 */
public class NodejsProcessManager {

	private final static NodejsProcessManager INSTANCE = new NodejsProcessManager();

	/**
	 * Returns the manager singleton.
	 * 
	 * @return
	 */
	public static NodejsProcessManager getInstance() {
		return INSTANCE;
	}

	/**
	 * List of node.js tern processes created.
	 */
	private final List<INodejsProcess> processes;

	/**
	 * The base dir where node.js Tern server is hosted.
	 */
	private File tsserverFile;

	/**
	 * Listener added for each process created.
	 */
	private final INodejsProcessListener listener = new NodejsProcessAdapter() {

		@Override
		public void onStart(INodejsProcess server) {
			synchronized (NodejsProcessManager.this.processes) {
				// here the process is started, add it to the list of processes.
				NodejsProcessManager.this.processes.add(server);
			}
		}

		@Override
		public void onStop(INodejsProcess server) {
			synchronized (NodejsProcessManager.this.processes) {
				// here the process is stopped, remove it to the list of
				// processes.
				NodejsProcessManager.this.processes.remove(server);
			}
		}

	};

	public NodejsProcessManager() {
		this.processes = new ArrayList<INodejsProcess>();
	}

	/**
	 * Create the process with the given tern project base dir where
	 * tsconfig.json is hosted. In this case the node exe used is the installed
	 * node. The tern server node.js must be initialized before with
	 * {@link NodejsProcessManager#init(File)}.
	 * 
	 * @param projectDir
	 *            project base dir where tsconfig.json is hosted.
	 * @return an instance of the node tern process.
	 * @throws TSException
	 */
	public INodejsProcess create(File projectDir) throws TSException {
		return create(projectDir, tsserverFile, null);
	}

	/**
	 * Create the process with the given tern project base dir where
	 * tsconfig.json is hosted and the given base dir of node.js exe. The tern
	 * server node.js must be initialized before with
	 * {@link NodejsProcessManager#init(File)}.
	 * 
	 * @param projectDir
	 *            project base dir where tsconfig.json is hosted.
	 * @param nodejsFile
	 *            the nodejs exe file
	 * @return an instance of the node tern process.
	 * @throws TSException
	 */
	public INodejsProcess create(File projectDir, File nodejsFile) throws TSException {
		return create(projectDir, null, nodejsFile);
	}

	/**
	 * Create the process with the given tern project base dir where
	 * tsconfig.json is hosted and the given base dir of node.js exe.
	 * 
	 * @param projectDir
	 *            project base dir where tsconfig.json is hosted.
	 * @param tsserverFile
	 *            the tsserver file.
	 * @param nodejsFile
	 *            the nodejs exe file
	 * @return an instance of the node tern process.
	 * @throws TSException
	 */
	public INodejsProcess create(File projectDir, File tsserverFile, File nodejsFile) throws TSException {
		INodejsProcess process = new NodeJSProcess(projectDir, tsserverFile, nodejsFile);
		process.addProcessListener(listener);
		return process;
	}

	/**
	 * Initialize the manager with the tsserver file. hosted.
	 * 
	 * @param tsserverFile
	 */
	public void init(File tsserverFile) {
		this.tsserverFile = tsserverFile;
	}

	/**
	 * Return the base dir where the tern node.js server is hosted.
	 * 
	 * @return
	 */
	public File getTsserverFile() {
		return tsserverFile;
	}

	/**
	 * Kill all node.js processes created by the manager.
	 */
	public void dispose() {
		synchronized (processes) {
			for (INodejsProcess server : processes) {
				try {
					server.kill();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			processes.clear();
		}
	}

}